"""
Run Ollama on Modal's GPU and expose it as an HTTPS endpoint that your LOCAL app calls.

Your Spring Boot app keeps running on your laptop (./mvnw spring-boot:run); it just talks to
this cloud Ollama instead of a local one. Nothing in the Java code changes.

A small FastAPI reverse proxy sits in front of Ollama and forwards /api/* calls to it. This is
deliberate: Modal's raw port proxy (web_server) mishandles Ollama's chunked responses
("chunked can not be set if Transfer-Encoding: chunked ..."). The proxy buffers each response and
returns a clean Content-Length, which avoids that error.

------------------------------------------------------------------------------------------------
DEPLOY (from the project folder, after `python -m modal token new`):

    cd "C:\\Users\\deevesh.beegun\\OneDrive - Accenture\\Desktop\\llm"
    python -m modal deploy deploy/ollama_modal.py

Modal prints a URL, e.g. https://<workspace>--ollama-wildlife-serve.modal.run
First deploy downloads the model into a Volume (~30-60s); wait a minute, then check:

    curl https://<workspace>--ollama-wildlife-serve.modal.run/api/tags     # should list the model

------------------------------------------------------------------------------------------------
The app already defaults to this endpoint (see application.yml), so just run:

    ./mvnw spring-boot:run

To override, set OLLAMA_BASE_URL / OLLAMA_CHAT_MODEL.

NOTES
* Model lives in a persistent Modal Volume: downloaded once, reused across restarts.
* min_containers=1 keeps one GPU container warm (~$0.60/hr for a T4). Set 0 to scale to zero
  (first call after idle pays a cold start), or `modal app stop ollama-wildlife`.
* The endpoint is PUBLIC. Fine for a demo; lock it down before anything real.
"""

import subprocess
import time

import modal

MODEL = "llama3.2:3b"
MODELS_DIR = "/models"

volume = modal.Volume.from_name("ollama-wildlife-models", create_if_missing=True)

image = (
    modal.Image.debian_slim()
    .apt_install("curl", "ca-certificates", "zstd")  # zstd: the Ollama installer needs it to extract
    .run_commands("curl -fsSL https://ollama.com/install.sh | sh")
    .pip_install("fastapi", "httpx")
    # OLLAMA_KEEP_ALIVE=-1 keeps the model resident in GPU memory so calls never pay a reload.
    .env({"OLLAMA_HOST": "127.0.0.1:11434", "OLLAMA_MODELS": MODELS_DIR, "OLLAMA_KEEP_ALIVE": "-1"})
)

app = modal.App("ollama-wildlife", image=image)


def _wait_until_ready(seconds: int = 180) -> None:
    for _ in range(seconds):
        if subprocess.run(["ollama", "list"], capture_output=True).returncode == 0:
            return
        time.sleep(1)


@app.function(gpu="T4", min_containers=1, timeout=86400, volumes={MODELS_DIR: volume})
@modal.asgi_app()
def serve():
    import httpx
    from fastapi import FastAPI, Request, Response

    subprocess.Popen(["ollama", "serve"])
    _wait_until_ready()
    if MODEL not in subprocess.run(["ollama", "list"], capture_output=True, text=True).stdout:
        subprocess.run(["ollama", "pull", MODEL], check=True)
        volume.commit()  # persist the model so later restarts skip the download

    # Warm the model into GPU memory during startup so the first real request is fast, not a
    # 60-90s cold load that would trip the client's read timeout.
    subprocess.run(["ollama", "run", MODEL, "ok"], capture_output=True, timeout=300)

    web = FastAPI()
    upstream = httpx.AsyncClient(base_url="http://127.0.0.1:11434", timeout=httpx.Timeout(600.0))
    hop_by_hop = {"host", "content-length", "transfer-encoding", "content-encoding", "connection"}

    @web.api_route("/{path:path}", methods=["GET", "POST", "PUT", "DELETE", "PATCH"])
    async def proxy(path: str, request: Request):
        body = await request.body()
        headers = {k: v for k, v in request.headers.items() if k.lower() not in hop_by_hop}
        result = await upstream.request(
            request.method, "/" + path, content=body, headers=headers, params=request.query_params
        )
        response_headers = {k: v for k, v in result.headers.items() if k.lower() not in hop_by_hop}
        return Response(
            content=result.content,
            status_code=result.status_code,
            headers=response_headers,
            media_type=result.headers.get("content-type"),
        )

    return web

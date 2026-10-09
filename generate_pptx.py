"""Generates a PowerPoint presentation for the Wildlife Form Extractor project."""

from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN
from pptx.util import Inches, Pt
import pptx.oxml.ns as nsmap
from lxml import etree

# ── Colour palette (Accenture-inspired) ──────────────────────────────────────
ACN_PURPLE  = RGBColor(0xA1, 0x00, 0xFF)   # Accenture purple
ACN_DARK    = RGBColor(0x1A, 0x1A, 0x2E)   # near-black navy
ACN_MID     = RGBColor(0x16, 0x21, 0x3E)   # dark blue
ACN_SLATE   = RGBColor(0x0F, 0x3F, 0x5C)   # mid-blue
ACN_ACCENT  = RGBColor(0x00, 0xC8, 0xFF)   # cyan highlight
WHITE       = RGBColor(0xFF, 0xFF, 0xFF)
LIGHT_GREY  = RGBColor(0xF0, 0xF0, 0xF5)
MID_GREY    = RGBColor(0xA0, 0xA0, 0xB0)
GREEN       = RGBColor(0x00, 0xD4, 0x7E)
ORANGE      = RGBColor(0xFF, 0x8C, 0x00)

SLIDE_W = Inches(13.33)
SLIDE_H = Inches(7.5)

prs = Presentation()
prs.slide_width  = SLIDE_W
prs.slide_height = SLIDE_H

blank_layout = prs.slide_layouts[6]   # completely blank


# ── helpers ──────────────────────────────────────────────────────────────────

def add_rect(slide, left, top, width, height, fill_color, alpha=None):
    shape = slide.shapes.add_shape(
        pptx.enum.shapes.MSO_SHAPE_TYPE.AUTO_SHAPE if False else 1,  # MSO_SHAPE_TYPE.RECTANGLE = 1
        left, top, width, height
    )
    shape.line.fill.background()
    shape.fill.solid()
    shape.fill.fore_color.rgb = fill_color
    return shape


def add_textbox(slide, text, left, top, width, height,
                font_size=18, bold=False, color=WHITE,
                align=PP_ALIGN.LEFT, italic=False, word_wrap=True):
    txBox = slide.shapes.add_textbox(left, top, width, height)
    tf = txBox.text_frame
    tf.word_wrap = word_wrap
    p = tf.paragraphs[0]
    p.alignment = align
    run = p.add_run()
    run.text = text
    run.font.size = Pt(font_size)
    run.font.bold = bold
    run.font.italic = italic
    run.font.color.rgb = color
    run.font.name = "Calibri"
    return txBox


def add_paragraph(tf, text, font_size=16, bold=False, color=WHITE,
                  align=PP_ALIGN.LEFT, italic=False, space_before=None):
    p = tf.add_paragraph()
    p.alignment = align
    if space_before:
        p.space_before = Pt(space_before)
    run = p.add_run()
    run.text = text
    run.font.size = Pt(font_size)
    run.font.bold = bold
    run.font.italic = italic
    run.font.color.rgb = color
    run.font.name = "Calibri"
    return p


def bullet_box(slide, items, left, top, width, height,
               font_size=15, color=WHITE, title=None, title_color=None,
               bg_color=None, padding=Inches(0.15)):
    if bg_color:
        add_rect(slide, left, top, width, height, bg_color)
    txBox = slide.shapes.add_textbox(
        left + padding, top + padding, width - 2*padding, height - 2*padding
    )
    tf = txBox.text_frame
    tf.word_wrap = True
    first = True
    if title:
        p = tf.paragraphs[0] if first else tf.add_paragraph()
        first = False
        p.alignment = PP_ALIGN.LEFT
        run = p.add_run()
        run.text = title
        run.font.size = Pt(font_size + 1)
        run.font.bold = True
        run.font.color.rgb = title_color or ACN_ACCENT
        run.font.name = "Calibri"
    for item in items:
        p = tf.paragraphs[0] if first else tf.add_paragraph()
        first = False
        p.alignment = PP_ALIGN.LEFT
        run = p.add_run()
        run.text = item
        run.font.size = Pt(font_size)
        run.font.color.rgb = color
        run.font.name = "Calibri"
    return txBox


def header_bar(slide, title, subtitle=None):
    """Dark header stripe across the top."""
    add_rect(slide, 0, 0, SLIDE_W, Inches(1.1), ACN_DARK)
    add_rect(slide, 0, Inches(1.1), Inches(0.07), SLIDE_H - Inches(1.1), ACN_PURPLE)
    add_textbox(slide, title,
                Inches(0.25), Inches(0.18), Inches(11), Inches(0.55),
                font_size=28, bold=True, color=WHITE, align=PP_ALIGN.LEFT)
    if subtitle:
        add_textbox(slide, subtitle,
                    Inches(0.25), Inches(0.7), Inches(11), Inches(0.38),
                    font_size=14, bold=False, color=ACN_ACCENT, align=PP_ALIGN.LEFT)


def footer(slide, page_num, total):
    add_rect(slide, 0, SLIDE_H - Inches(0.35), SLIDE_W, Inches(0.35), ACN_DARK)
    add_textbox(slide,
                f"Wildlife Form Extractor  |  Accenture   •   {page_num} / {total}",
                Inches(0.3), SLIDE_H - Inches(0.32), Inches(10), Inches(0.3),
                font_size=9, color=MID_GREY, align=PP_ALIGN.LEFT)
    add_textbox(slide, "CONFIDENTIAL",
                Inches(10.5), SLIDE_H - Inches(0.32), Inches(2.5), Inches(0.3),
                font_size=9, color=MID_GREY, align=PP_ALIGN.RIGHT)


TOTAL_SLIDES = 10

# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 1 — Title
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_DARK)

# big accent bar on the left
add_rect(slide, 0, 0, Inches(0.18), SLIDE_H, ACN_PURPLE)

# decorative horizontal rule
add_rect(slide, Inches(0.4), Inches(3.15), Inches(6.8), Inches(0.06), ACN_PURPLE)

add_textbox(slide, "Wildlife Form Extractor",
            Inches(0.4), Inches(1.1), Inches(12), Inches(1.2),
            font_size=48, bold=True, color=WHITE, align=PP_ALIGN.LEFT)

add_textbox(slide, "Speech-to-Structured-Data for Wildlife Field Observations",
            Inches(0.4), Inches(2.45), Inches(10), Inches(0.7),
            font_size=22, bold=False, color=ACN_ACCENT, align=PP_ALIGN.LEFT)

add_textbox(slide,
            "Java 17  •  Spring Boot 4.1  •  Spring AI 2.0  •  Ollama LLM  •  Docker",
            Inches(0.4), Inches(3.35), Inches(10), Inches(0.45),
            font_size=14, color=MID_GREY, align=PP_ALIGN.LEFT)

add_textbox(slide, "Accenture  |  2026",
            Inches(0.4), Inches(6.65), Inches(5), Inches(0.4),
            font_size=13, color=MID_GREY, align=PP_ALIGN.LEFT)

# decorative dots
for i in range(5):
    add_rect(slide, Inches(10.5 + i * 0.45), Inches(6.5), Inches(0.22), Inches(0.22), ACN_PURPLE)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 2 — Problem Statement
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "The Problem", "Turning spoken wildlife observations into usable data is slow and error-prone")
footer(slide, 2, TOTAL_SLIDES)

# three "pain point" cards
cards = [
    ("Manual Transcription", [
        "Field officers dictate observations verbally",
        "Data entry clerks re-type into structured forms",
        "Bottleneck of hours → days before data is usable",
    ]),
    ("Inconsistent Data Quality", [
        "Free-text entries miss required fields",
        "Species names & nest IDs spelled inconsistently",
        "No automated validation against reference data",
    ]),
    ("Six Different Form Types", [
        "Sighting, Feeding, Nest Site, Eggs/Chick,",
        "Competitors & Predators, Ringing & Morphs",
        "Each has its own schema & vocabulary rules",
    ]),
]
for i, (title, bullets) in enumerate(cards):
    x = Inches(0.4 + i * 4.25)
    add_rect(slide, x, Inches(1.4), Inches(4.0), Inches(4.8), ACN_DARK)
    add_rect(slide, x, Inches(1.4), Inches(4.0), Inches(0.07), ACN_PURPLE)
    add_textbox(slide, title,
                x + Inches(0.18), Inches(1.55), Inches(3.6), Inches(0.5),
                font_size=17, bold=True, color=ACN_ACCENT)
    y = Inches(2.2)
    for b in bullets:
        add_textbox(slide, f"• {b}",
                    x + Inches(0.18), y, Inches(3.6), Inches(0.55),
                    font_size=13, color=WHITE)
        y += Inches(0.55)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 3 — Solution Overview
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "Solution Overview", "An LLM-assisted extraction pipeline with deterministic guardrails")
footer(slide, 3, TOTAL_SLIDES)

add_textbox(slide,
            "The Wildlife Form Extractor accepts a speech-to-text transcript and returns a validated, structured JSON "
            "payload ready to populate any of six wildlife observation forms — with a mandatory human-confirmation step "
            "before any data is persisted.",
            Inches(0.35), Inches(1.25), Inches(12.6), Inches(0.85),
            font_size=14, color=WHITE)

# flow boxes
flow = [
    (ACN_PURPLE, "1. Receive", "REST POST\n/api/v1/wildlife-extractions"),
    (ACN_SLATE,  "2. Extract", "LLM (Ollama)\nStructured output"),
    (ACN_SLATE,  "3. Validate", "Schema + Vocab\n+ References\n+ Business rules"),
    (ACN_SLATE,  "4. Decide", "AUTO / REVIEW\n/ MORE INFO\n/ REJECT"),
    (GREEN,      "5. Confirm", "Human POST\n/{id}/confirm"),
]
arrow_color = ACN_ACCENT
for i, (color, title, body) in enumerate(flow):
    bx = Inches(0.35 + i * 2.55)
    add_rect(slide, bx, Inches(2.3), Inches(2.35), Inches(2.55), color)
    add_textbox(slide, title,
                bx + Inches(0.12), Inches(2.42), Inches(2.1), Inches(0.42),
                font_size=15, bold=True, color=WHITE, align=PP_ALIGN.CENTER)
    add_textbox(slide, body,
                bx + Inches(0.1), Inches(2.9), Inches(2.15), Inches(1.6),
                font_size=12, color=LIGHT_GREY, align=PP_ALIGN.CENTER)
    if i < len(flow) - 1:
        add_textbox(slide, "→",
                    bx + Inches(2.35), Inches(2.95), Inches(0.22), Inches(0.5),
                    font_size=22, bold=True, color=ACN_ACCENT, align=PP_ALIGN.CENTER)

add_textbox(slide,
            "Key design principle: the LLM proposes field values — deterministic Java code validates, resolves, decides, and stores.",
            Inches(0.35), Inches(5.2), Inches(12.6), Inches(0.5),
            font_size=13, italic=True, color=ACN_ACCENT, align=PP_ALIGN.CENTER)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 4 — Architecture
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "Architecture", "Clean layered design with strict dependency inversion")
footer(slide, 4, TOTAL_SLIDES)

# Package layers diagram (text-based)
layers = [
    ("api",            "Controllers · Request/Response DTOs · ProblemDetail advice",  ACN_PURPLE),
    ("application",    "Orchestration · Validation · Decision · References · Vocab · Confirmation",  ACN_SLATE),
    ("domain",         "Form types · Field records · Extraction/Reference/Vocab/Validation model",  RGBColor(0x0A, 0x52, 0x6B)),
    ("infrastructure", "Spring AI/Ollama · Schema · Persistence · Resilience · Observability · RAG",  ACN_DARK),
    ("configuration",  "Typed @ConfigurationProperties · Jackson pipeline mapper",   RGBColor(0x2A, 0x2A, 0x4A)),
]
for i, (name, desc, color) in enumerate(layers):
    y = Inches(1.35 + i * 0.93)
    add_rect(slide, Inches(0.4), y, Inches(8.5), Inches(0.82), color)
    add_textbox(slide, name,
                Inches(0.6), y + Inches(0.08), Inches(2.0), Inches(0.4),
                font_size=15, bold=True, color=ACN_ACCENT)
    add_textbox(slide, desc,
                Inches(2.75), y + Inches(0.18), Inches(6.0), Inches(0.45),
                font_size=12, color=WHITE)

# right panel — key principles
add_rect(slide, Inches(9.2), Inches(1.35), Inches(3.85), Inches(4.6), ACN_DARK)
add_rect(slide, Inches(9.2), Inches(1.35), Inches(3.85), Inches(0.07), ACN_PURPLE)
principles = [
    "No outward dependencies from",
    "domain or application layers",
    "",
    "LLM interaction isolated in",
    "infrastructure/springai/ollama",
    "",
    "Resilience: circuit breaker,",
    "bulkhead, timeout, retries",
    "",
    "In-memory stores (by design —",
    "replaceable via interfaces)",
    "",
    "Optional RAG extension point",
    "(disabled by default)",
]
add_textbox(slide, "Design Principles",
            Inches(9.4), Inches(1.45), Inches(3.4), Inches(0.45),
            font_size=15, bold=True, color=ACN_ACCENT)
y = Inches(2.0)
for line in principles:
    add_textbox(slide, line,
                Inches(9.4), y, Inches(3.4), Inches(0.32),
                font_size=11.5, color=WHITE if line else WHITE)
    y += Inches(0.28)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 5 — Supported Forms & Fields
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "Six Supported Observation Forms", "Each form has its own schema, prompt, and vocabulary rules")
footer(slide, 5, TOTAL_SLIDES)

forms = [
    ("SIGHTING",                  "Species, date/time, location, observer,\ncount, behaviour, coordinates"),
    ("FEEDING_OBSERVATION",       "Species, food plant, feeding tree,\nduration, feeding behaviour"),
    ("NEST_SITE_CHARACTERISTICS", "Nest ID, tree species, height, cavity,\ntype, substrate, GPS coords"),
    ("NEST_EGGS_CHICK",           "Eggs (count, state, dates),\nChicks (count, condition, hatch date)"),
    ("COMPETITORS_AND_PREDATORS", "Competitor/predator species,\ninteraction type, frequency, severity"),
    ("RINGING_MORPHS",            "Ring ID, morph type, colour,\nbill/tarsus/wing measurements"),
]
for i, (name, fields) in enumerate(forms):
    col = i % 3
    row = i // 3
    bx = Inches(0.35 + col * 4.3)
    by = Inches(1.4 + row * 2.6)
    add_rect(slide, bx, by, Inches(4.1), Inches(2.35), ACN_DARK)
    add_rect(slide, bx, by, Inches(0.06), Inches(2.35), ACN_PURPLE)
    add_textbox(slide, name,
                bx + Inches(0.18), by + Inches(0.12), Inches(3.8), Inches(0.45),
                font_size=13, bold=True, color=ACN_ACCENT)
    add_textbox(slide, fields,
                bx + Inches(0.18), by + Inches(0.62), Inches(3.8), Inches(1.5),
                font_size=11.5, color=WHITE)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 6 — Validation Pipeline
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "Validation Pipeline", "Nine deterministic stages — the LLM never decides anything binding")
footer(slide, 6, TOTAL_SLIDES)

steps = [
    ("Request Validation",    "Size, required fields, form type check"),
    ("Model Response Size",   "Hard limit on raw LLM output bytes"),
    ("JSON Parsing",          "Independent parse into JsonNode"),
    ("JSON Schema Validation","Draft 2020-12, additionalProperties:false"),
    ("DTO Mapping",           "Jackson → typed ExtractionFields POJO"),
    ("Jakarta Bean Validation","@NotNull / @Size / pattern constraints"),
    ("Vocabulary Validation", "OPEN/CLOSED vocabularies + alias mapping"),
    ("Reference Resolution",  "Species, nest, user IDs via YAML refs"),
    ("Business Validation",   "Cross-field logic & contradiction checks"),
]
for i, (stage, desc) in enumerate(steps):
    col = i % 3
    row = i // 3
    bx = Inches(0.3 + col * 4.35)
    by = Inches(1.38 + row * 1.72)
    # number circle
    add_rect(slide, bx, by + Inches(0.25), Inches(0.42), Inches(0.42), ACN_PURPLE)
    add_textbox(slide, str(i + 1),
                bx, by + Inches(0.2), Inches(0.42), Inches(0.42),
                font_size=13, bold=True, color=WHITE, align=PP_ALIGN.CENTER)
    add_rect(slide, bx + Inches(0.5), by, Inches(3.75), Inches(1.38), ACN_DARK)
    add_textbox(slide, stage,
                bx + Inches(0.65), by + Inches(0.1), Inches(3.4), Inches(0.45),
                font_size=13, bold=True, color=ACN_ACCENT)
    add_textbox(slide, desc,
                bx + Inches(0.65), by + Inches(0.58), Inches(3.4), Inches(0.62),
                font_size=11.5, color=WHITE)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 7 — Decision Engine & Correction Flow
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "Decision Engine & Self-Correction", "Four outcomes — with automatic repair for structural failures")
footer(slide, 7, TOTAL_SLIDES)

# Decision cards
decisions = [
    (GREEN,  "ACCEPT\nAUTOMATICALLY",  "All required values valid\nNo ambiguity or contradiction\nAll required refs resolved"),
    (ORANGE, "REQUEST MORE\nINFORMATION", "Required fields absent\nNeeds re-submission with\nmore detail"),
    (RGBColor(0xFF, 0x45, 0x00), "MANUAL\nREVIEW",    "Ambiguity / contradiction\nInvalid field value\nUnresolved required reference"),
    (RGBColor(0xCC, 0x00, 0x00), "REJECT",            "Malformed / unsafe output\nCorrection exhausted\nStructurally unrecoverable"),
]
for i, (color, title, body) in enumerate(decisions):
    bx = Inches(0.3 + i * 3.22)
    add_rect(slide, bx, Inches(1.38), Inches(3.0), Inches(2.8), ACN_DARK)
    add_rect(slide, bx, Inches(1.38), Inches(3.0), Inches(0.08), color)
    add_textbox(slide, title,
                bx + Inches(0.15), Inches(1.5), Inches(2.7), Inches(0.72),
                font_size=14, bold=True, color=color, align=PP_ALIGN.CENTER)
    add_textbox(slide, body,
                bx + Inches(0.15), Inches(2.3), Inches(2.7), Inches(1.5),
                font_size=12, color=WHITE, align=PP_ALIGN.CENTER)

# Correction flow
add_rect(slide, Inches(0.3), Inches(4.45), Inches(12.75), Inches(2.3), ACN_DARK)
add_rect(slide, Inches(0.3), Inches(4.45), Inches(12.75), Inches(0.07), ACN_ACCENT)
add_textbox(slide, "Self-Correction Loop  (structural failures only)",
            Inches(0.5), Inches(4.55), Inches(12.3), Inches(0.45),
            font_size=14, bold=True, color=ACN_ACCENT)

correction_steps = [
    "Model output", "→", "Structurally valid?", "→  YES →", "Continue pipeline",
]
add_textbox(slide,
            "Model output  →  Structurally valid?  →  YES  →  Continue pipeline\n"
            "                             ↓ NO\n"
            "                   Corrections remaining?  →  NO  →  REJECT\n"
            "                             ↓ YES\n"
            "           One correction call: transcript + invalid JSON + sanitised errors + schema\n"
            "                             ↓\n"
            "                        Valid now?  →  YES  →  Continue pipeline  |  NO  →  REJECT",
            Inches(0.5), Inches(5.05), Inches(12.3), Inches(1.55),
            font_size=12, color=WHITE)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 8 — Security & Reliability
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "Security & Reliability", "Production-hardened from day one")
footer(slide, 8, TOTAL_SLIDES)

security = [
    "No transcript / prompt / response logging",
    "Sanitised schema errors (no instance values)",
    "Correlation IDs via X-Correlation-Id header",
    "Max HTTP body / transcript / response sizes",
    "No tool calling, URL fetching, or command exec from transcripts",
    "No persistence of invalid or rejected output",
    "Prompt-injection text treated as ordinary observation data",
]

reliability = [
    "Circuit breaker — trips on transport failures only",
    "Bulkhead — bounded concurrent calls + wait queue",
    "Admission bound — excess calls → 503",
    "Per-call execution timeout",
    "Retries limited to safe transient failures",
    "Validation / business failures are never retried",
    "Graceful executor shutdown on context close",
]

for i, (title, items, color) in enumerate([
    ("Security", security, RGBColor(0xFF, 0x45, 0x00)),
    ("Reliability", reliability, GREEN),
]):
    bx = Inches(0.35 + i * 6.55)
    add_rect(slide, bx, Inches(1.35), Inches(6.3), Inches(5.4), ACN_DARK)
    add_rect(slide, bx, Inches(1.35), Inches(6.3), Inches(0.07), color)
    add_textbox(slide, title,
                bx + Inches(0.2), Inches(1.45), Inches(5.9), Inches(0.5),
                font_size=18, bold=True, color=color)
    y = Inches(2.1)
    for item in items:
        add_textbox(slide, f"✓  {item}",
                    bx + Inches(0.2), y, Inches(5.9), Inches(0.48),
                    font_size=12.5, color=WHITE)
        y += Inches(0.53)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 9 — Technology Stack
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_MID)
header_bar(slide, "Technology Stack", "Modern, standards-compliant, container-ready")
footer(slide, 9, TOTAL_SLIDES)

tech_groups = [
    ("Runtime & Framework", [
        "Java 17  (targets JDK ≥ 17)",
        "Spring Boot 4.1.0",
        "Spring Web MVC",
        "Spring AI 2.0.0 (Ollama adapter)",
    ]),
    ("AI & LLM", [
        "Ollama — local LLM server",
        "llama3.1:8b (default model)",
        "Temperature 0, no tools/streaming",
        "Native structured-output via format option",
    ]),
    ("Validation", [
        "networknt JSON Schema Draft 2020-12",
        "Jakarta Bean Validation",
        "Custom vocabulary YAML engine",
        "Deterministic reference resolvers",
    ]),
    ("Resilience & Observability", [
        "Resilience4j (CB + bulkhead + timeout + retry)",
        "Micrometer + Prometheus metrics",
        "Spring Actuator (health, liveness, readiness)",
        "Structured low-cardinality tags",
    ]),
    ("Serialisation & Build", [
        "Jackson 3 (web layer) + Jackson 2 (pipeline)",
        "Maven Wrapper",
        "Docker + Docker Compose",
        "Render.yaml cloud deploy config",
    ]),
    ("Testing", [
        "JUnit 5 + Mockito",
        "MockMvc (controller slices)",
        "OkHttp MockWebServer",
        "Evaluation fixtures (all 6 form types)",
    ]),
]

for i, (group, items) in enumerate(tech_groups):
    col = i % 3
    row = i // 3
    bx = Inches(0.3 + col * 4.35)
    by = Inches(1.38 + row * 2.6)
    add_rect(slide, bx, by, Inches(4.1), Inches(2.35), ACN_DARK)
    add_rect(slide, bx, by, Inches(4.1), Inches(0.06), ACN_PURPLE)
    add_textbox(slide, group,
                bx + Inches(0.15), by + Inches(0.1), Inches(3.8), Inches(0.42),
                font_size=13, bold=True, color=ACN_ACCENT)
    y = by + Inches(0.6)
    for item in items:
        add_textbox(slide, f"• {item}",
                    bx + Inches(0.15), y, Inches(3.8), Inches(0.38),
                    font_size=11.5, color=WHITE)
        y += Inches(0.41)


# ═══════════════════════════════════════════════════════════════════════════════
# SLIDE 10 — Summary & Next Steps
# ═══════════════════════════════════════════════════════════════════════════════
slide = prs.slides.add_slide(blank_layout)
add_rect(slide, 0, 0, SLIDE_W, SLIDE_H, ACN_DARK)
add_rect(slide, 0, 0, Inches(0.18), SLIDE_H, ACN_PURPLE)
add_rect(slide, Inches(0.4), Inches(1.05), Inches(6.8), Inches(0.06), ACN_PURPLE)
footer(slide, 10, TOTAL_SLIDES)

add_textbox(slide, "Summary & Next Steps",
            Inches(0.4), Inches(0.18), Inches(10), Inches(0.75),
            font_size=32, bold=True, color=WHITE)

# Summary bullets
summary = [
    "Converts noisy spoken observations into validated, structured JSON",
    "Six form types, nine validation stages, four decision outcomes",
    "LLM never decides, never stores, never touches references or identifiers",
    "Production-grade resilience, security, and observability built in",
    "Fully containerised — deploy via Docker Compose or Render",
]
add_textbox(slide, "What we built",
            Inches(0.5), Inches(1.25), Inches(6.0), Inches(0.45),
            font_size=16, bold=True, color=ACN_ACCENT)
y = Inches(1.78)
for s in summary:
    add_textbox(slide, f"✓  {s}",
                Inches(0.5), y, Inches(5.9), Inches(0.45),
                font_size=12.5, color=WHITE)
    y += Inches(0.48)

# Next steps
next_steps = [
    "Extend required-field policy beyond species",
    "Implement RAG vector-store for terminology hints",
    "Persistent / clustered reference & idempotency stores",
    "Add more LLM providers (OpenAI, Anthropic Claude)",
    "Expose evaluation runner as a CI pipeline step",
    "Mobile app integration via REST client",
]
add_rect(slide, Inches(7.2), Inches(1.25), Inches(5.8), Inches(5.35), ACN_MID)
add_rect(slide, Inches(7.2), Inches(1.25), Inches(5.8), Inches(0.07), ACN_ACCENT)
add_textbox(slide, "Roadmap",
            Inches(7.4), Inches(1.38), Inches(5.4), Inches(0.45),
            font_size=16, bold=True, color=ACN_ACCENT)
y = Inches(1.95)
for n in next_steps:
    add_textbox(slide, f"→  {n}",
                Inches(7.4), y, Inches(5.3), Inches(0.48),
                font_size=12.5, color=WHITE)
    y += Inches(0.5)


# ── Save ──────────────────────────────────────────────────────────────────────
out = r"C:\Users\deevesh.beegun\OneDrive - Accenture\Desktop\project\birdLLMJ\WildlifeFormExtractor.pptx"
prs.save(out)
print(f"Saved: {out}")

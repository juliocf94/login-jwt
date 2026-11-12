"""Genera los PDF de docs/ a partir de los .md (Chrome headless).

Uso:  pip install markdown pygments pypdf
      python docs/build_pdf.py            -> genera todos
      python docs/build_pdf.py pasos      -> solo uno
"""
import pathlib, subprocess, sys
import markdown

DOCS = pathlib.Path(__file__).resolve().parent
CHROME = r"C:\Program Files\Google\Chrome\Application\chrome.exe"

CHIPS = ('<div class="chips"><span class="chip" style="background:#e76f00">Java 21</span>'
         '<span class="chip" style="background:#c74634">Oracle</span><span class="chip" style="background:#c3002f">Angular 22</span>'
         '<span class="chip" style="background:#1f3a5f">JWT</span><span class="chip" style="background:#6db33f">Spring Boot</span>'
         '<span class="chip" style="background:#4695eb">Quarkus</span></div>')

DOCUMENTOS = {
    "preguntas": {
        "md": "PREGUNTAS-TEORIA.md", "pdf": "Preguntas-Teoria.pdf", "pie": "Preguntas de teoría · Java · Oracle · Angular · JWT",
        "kicker": "Prueba técnica · Banco de preguntas", "titulo": "Teoría: Java,<br>Oracle y Angular",
        "sub": "10 preguntas por tema con respuesta corta y una analogía con PHP / Laravel / Node / React. "
               "Las 1 a 5 son las básicas y de la 6 a la 10, alternativas esenciales. Al final, 5 extra de JWT y Spring.",
        "css": "h2 { break-before: page; font-size: 20pt; border-bottom: 3px solid var(--navy); padding-bottom: 4px; }  h2.primero { break-before: avoid; }",
    },
    "pasos": {
        "md": "PASOS-PRUEBA.md", "pdf": "Pasos-Prueba.pdf", "pie": "Pasos y comandos · login-jwt",
        "kicker": "Prueba técnica · Día de la prueba", "titulo": "Pasos y comandos<br>para levantar todo",
        "sub": "Del git clone al login funcionando: Oracle en Docker, backend Quarkus o Spring Boot, Angular y pruebas en Swagger. "
               "Cada paso indica cómo saber que funcionó y qué hacer si falla. Al final, un resumen de una página.",
        "css": "h2 { break-before: auto; margin-top: 26px; }  pre { break-inside: avoid; }  table { break-inside: avoid; }",
    },
    "guia": {
        "md": ["GUIA-JWT.md", "MAPA-ARCHIVOS.md"], "pdf": "Guia-JWT-y-Codigo.pdf", "pie": "Cómo funciona el login y el código · login-jwt",
        "kicker": "Prueba técnica · Entender tu propio proyecto", "titulo": "Login con JWT<br>y mapa del código",
        "sub": "Cómo viaja el token desde el login hasta cada petición, explicado con la analogía de la pulsera de hotel. "
               "Quarkus y Spring lado a lado, qué hace cada archivo y las preguntas típicas sobre seguridad.",
        "css": "h2 { break-before: auto; margin-top: 24px; }  pre { break-inside: avoid; }  table { break-inside: avoid; }",
    },
}


def construir(clave: str) -> None:
    cfg = DOCUMENTOS[clave]
    html_tmp, pdf = DOCS / f"_{clave}.html", DOCS / cfg["pdf"]
    fuentes = cfg["md"] if isinstance(cfg["md"], list) else [cfg["md"]]   # uno o varios .md
    texto = "\n\n".join((DOCS / f).read_text(encoding="utf-8") for f in fuentes)
    body = markdown.markdown(texto,
                             extensions=["tables", "fenced_code", "codehilite", "attr_list"]
                                        + (["nl2br"] if clave == "preguntas" else []),   # pregunta / respuesta / ≈ en líneas separadas
                             extension_configs={"codehilite": {"guess_lang": False, "noclasses": True, "pygments_style": "friendly"}})
    body = body.replace("<h1>", '<h1 class="doc">', 1).replace("<h2>", '<h2 class="primero">', 1)

    portada = (f'<div class="portada"><div class="kicker">{cfg["kicker"]}</div><div class="titulo-grande">{cfg["titulo"]}</div>'
               f'<div class="sub">{cfg["sub"]}</div>{CHIPS}'
               '<div class="meta">Proyecto: <code>login-jwt</code> (login con JWT, roles ADMIN y VIEW, CRUD sobre Oracle)</div></div>')
    css = (DOCS / "pdf-style.css").read_text(encoding="utf-8")
    css += f'\n@page {{ @bottom-left {{ content: "{cfg["pie"]}"; }} }}\n{cfg["css"]}'

    html_tmp.write_text(f'<!doctype html><html lang="es"><head><meta charset="utf-8"><title>{cfg["pdf"]}</title>'
                        f"<style>{css}</style></head><body>{portada}{body}</body></html>", encoding="utf-8")
    pdf.unlink(missing_ok=True)
    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--no-pdf-header-footer",
                    f"--user-data-dir={DOCS / '.chrome-profile'}", f"--print-to-pdf={pdf}", html_tmp.as_uri()],
                   capture_output=True, timeout=180)
    html_tmp.unlink()
    if not pdf.exists():
        sys.exit(f"No se generó {pdf.name}")
    from pypdf import PdfReader
    print(f"{pdf.name}: {len(PdfReader(str(pdf)).pages)} páginas")


if __name__ == "__main__":
    for clave in (sys.argv[1:] or DOCUMENTOS):
        construir(clave)

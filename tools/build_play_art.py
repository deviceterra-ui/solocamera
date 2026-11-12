"""Render deterministic Play listing graphics from the app's dark camera palette."""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

OUT = Path(__file__).resolve().parents[1] / "docs" / "play" / "assets"
OUT.mkdir(parents=True, exist_ok=True)


def font(size, bold=False):
    face = "segoeuib.ttf" if bold else "segoeui.ttf"
    return ImageFont.truetype(f"C:/Windows/Fonts/{face}", size)


def camera(draw, box, color, width):
    x0, y0, x1, y1 = box
    h = y1 - y0
    draw.rounded_rectangle((x0, y0 + h * .16, x1, y1), radius=h * .13, outline=color, width=width)
    draw.rounded_rectangle((x0 + h * .25, y0, x0 + h * .64, y0 + h * .22), radius=h * .04, fill=color)
    cx, cy = (x0 + x1) / 2, y0 + h * .59
    r = h * .23
    draw.ellipse((cx-r, cy-r, cx+r, cy+r), outline=color, width=width)
    draw.ellipse((cx-r*.44, cy-r*.44, cx+r*.44, cy+r*.44), fill=color)


icon = Image.new("RGB", (512, 512), "#101519")
d = ImageDraw.Draw(icon)
d.rounded_rectangle((35, 35, 477, 477), radius=96, fill="#1c252c", outline="#39444d", width=3)
d.ellipse((70, 70, 445, 445), outline="#2e6259", width=3)
camera(d, (126, 164, 386, 366), "#9de3cb", 19)
icon.convert("RGBA").save(OUT / "icon-512.png", optimize=True)

banner = Image.new("RGB", (1024, 500), "#101519")
pixels = banner.load()
for y in range(500):
    for x in range(1024):
        glow = max(0, 1 - ((x-865)**2 / 560**2 + (y-230)**2 / 410**2))
        pixels[x, y] = (int(16+9*glow), int(21+37*glow), int(25+34*glow))
d = ImageDraw.Draw(banner)
d.rounded_rectangle((43, 40, 981, 460), radius=34, outline="#344f50", width=2)
d.text((91, 91), "SOLO CAMERA", font=font(64, True), fill="#f2f5f3")
d.text((94, 178), "Shoot solo. Create freely.", font=font(34), fill="#9de3cb")
d.rounded_rectangle((93, 316, 540, 378), radius=23, fill="#243a38")
d.text((119, 325), "CAMERA  •  MONITOR  •  STUDIO", font=font(22, True), fill="#d2eee3")
d.ellipse((634, 83, 916, 365), outline="#426f68", width=4)
camera(d, (677, 160, 878, 325), "#9de3cb", 15)
banner.save(OUT / "feature-1024x500.png", optimize=True)

from pathlib import Path

from PIL import Image, ImageEnhance, ImageFilter, ImageOps, ImageChops


ROOT = Path(r"D:\Program\ANDROIDSTUDIO\WORKSPACE\ZhiGu_app")
PIC = ROOT / "pic"
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"

TARGET_SIZE = (1600, 900)


def crop_to_ratio(image: Image.Image, ratio: float = 16 / 9) -> Image.Image:
    width, height = image.size
    current = width / height
    if current > ratio:
        new_width = int(height * ratio)
        left = (width - new_width) // 2
        return image.crop((left, 0, left + new_width, height))
    new_height = int(width / ratio)
    top = (height - new_height) // 2
    return image.crop((0, top, width, top + new_height))


def vignette_mask(size: tuple[int, int]) -> Image.Image:
    width, height = size
    mask = Image.new("L", size, 255)
    inner = Image.new("L", (width - 180, height - 140), 150)
    inset_x = (width - inner.width) // 2
    inset_y = (height - inner.height) // 2
    mask.paste(inner, (inset_x, inset_y))
    return mask.filter(ImageFilter.GaussianBlur(90))


def grade(image: Image.Image) -> Image.Image:
    image = ImageOps.exif_transpose(image).convert("RGB")
    image = crop_to_ratio(image).resize(TARGET_SIZE, Image.Resampling.LANCZOS)
    image = ImageEnhance.Contrast(image).enhance(1.08)
    image = ImageEnhance.Color(image).enhance(0.92)
    image = ImageEnhance.Sharpness(image).enhance(1.18)

    cool_overlay = Image.new("RGB", image.size, (210, 220, 232))
    image = Image.blend(image, cool_overlay, 0.08)

    shadow = Image.new("RGB", image.size, (36, 46, 60))
    mask = vignette_mask(image.size)
    image = Image.composite(image, shadow, mask)

    image = ImageEnhance.Brightness(image).enhance(1.03)
    return image


def process(name_in: str, name_out: str) -> None:
    source = PIC / name_in
    target = OUT / name_out
    graded = grade(Image.open(source))
    graded.save(target, quality=95)


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    process("45ceca6c7137a4782572a731ba506587.jpg", "assist_scene_overview.jpg")
    process("52ec90ad6ba702bc059f4b1398eab1a6.jpg", "assist_scene_side.jpg")
    process("60c6416cd53e615b9e445bb33380b316.jpg", "assist_scene_top.jpg")


if __name__ == "__main__":
    main()

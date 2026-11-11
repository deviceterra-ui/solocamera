"""Build the illustrated, version-specific creator handbook; no app data is bundled."""
from pathlib import Path
import re, json, html, runpy
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import Paragraph, Spacer, KeepTogether
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.utils import ImageReader
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
(ROOT/'tmp/pdfs').mkdir(parents=True,exist_ok=True)
OUT=ROOT/'output/pdf/Solo-Camera-Complete-User-Manual-v0.52.1.pdf'
SCREENS=ROOT/'docs/manual/screens'
for name,file in [('UI','segoeui.ttf'),('UI-Bold','segoeuib.ttf'),('UI-Italic','segoeuii.ttf')]:
    pdfmetrics.registerFont(TTFont(name,str(Path('C:/Windows/Fonts')/file)))
pdfmetrics.registerFontFamily('UI',normal='UI',bold='UI-Bold',italic='UI-Italic',boldItalic='UI-Bold')
W,H=595.276,841.89
INK=colors.HexColor('#162434'); MUTED=colors.HexColor('#526477'); GOLD=colors.HexColor('#F2B83F'); LIGHT=colors.HexColor('#F3F6F8')
styles={
 'body':ParagraphStyle('body',fontName='UI',fontSize=10.4,leading=15.5,textColor=INK,spaceAfter=9),
 'step':ParagraphStyle('step',fontName='UI',fontSize=10.4,leading=15.5,textColor=INK,spaceAfter=9,leftIndent=14,firstLineIndent=-14),
 'heading':ParagraphStyle('heading',fontName='UI-Bold',fontSize=11.8,leading=15.5,textColor=INK,spaceBefore=9,spaceAfter=6),
 'note':ParagraphStyle('note',fontName='UI',fontSize=9.7,leading=14.2,textColor=INK,backColor=LIGHT,borderPadding=10,spaceBefore=9,spaceAfter=15),
 'caption':ParagraphStyle('caption',fontName='UI',fontSize=8.6,leading=12,textColor=MUTED),
}
pages=[]
def add(section,title,image,text,caption='Actual Solo Camera screen. Example content is for demonstration.'):
    pages.append(dict(section=section,title=title,image=image,text=text.strip(),caption=caption))
runpy.run_path(str(ROOT/'tools/manual_content.py'),init_globals={'add':add})

def markup(s):
    s=html.escape(s)
    return re.sub(r'\*\*(.+?)\*\*',r'<b>\1</b>',s)
def flowables(text,scale=1):
    result=[]
    for block in text.split('\n\n'):
        block=block.strip()
        if not block: continue
        if block.startswith('H|'): key='heading'; block=block[2:]
        elif block.startswith('N|'): key='note';block=block[2:]
        else:key='step' if re.match(r'^(\d+\.|-) ',block) else 'body'
        style=ParagraphStyle('local',parent=styles[key],fontSize=styles[key].fontSize*scale,leading=styles[key].leading*scale)
        result.append(Paragraph(markup(block).replace('\n','<br/>'),style))
    return result
def draw_body(c,items,x,y,width,bottom):
    for item in items:
        aw,ah=item.wrap(width,y-bottom)
        before=item.getSpaceBefore();after=item.getSpaceAfter()
        y-=before
        if y-ah<bottom: return y-ah
        item.drawOn(c,x,y-ah);y-=ah+after
    return y
def chrome(c,section,number):
    c.setFillColor(INK);c.setFont('UI-Bold',9);c.drawString(42,H-36,'SOLO CAMERA')
    c.setFillColor(MUTED);c.setFont('UI',8);c.drawRightString(W-42,H-36,section.upper())
    c.setStrokeColor(colors.HexColor('#DEE5EB'));c.line(42,49,W-42,49)
    c.setFont('UI',8);c.drawString(42,31,'Creator manual  |  Android 0.52.1')
    c.drawRightString(W-42,31,f'{number:02d}')
    c.setFont('UI-Bold',8);c.drawCentredString(W/2,31,'CONTENTS')
    c.linkRect('', 'contents', (W/2-30,25,W/2+30,42),relative=0,thickness=0)
def image_panel(c,key,x,y,width,caption):
    f=SCREENS/(key+'.png')
    if not f.exists(): raise FileNotFoundError(f)
    with Image.open(f) as im:iw,ih=im.size
    height=min(470,width*ih/iw);width=height*iw/ih
    c.setFillColor(colors.HexColor('#101923'));c.roundRect(x-5,y-height-5,width+10,height+10,10,fill=1,stroke=0)
    c.drawImage(ImageReader(str(f)),x,y-height,width,height,mask='auto')
    p=Paragraph(markup(caption),styles['caption']);_,ph=p.wrap(width,100);p.drawOn(c,x,y-height-22-ph)

OUT.parent.mkdir(parents=True,exist_ok=True)
c=canvas.Canvas(str(OUT),pagesize=(W,H),pageCompression=1)
c.setTitle('Solo Camera - Complete Creator User Manual | Android 0.52.1')
c.setAuthor('Solo Camera');c.setSubject('Illustrated camera, monitoring, screen capture, editing, audio and delivery guide')
c.setFillColor(colors.HexColor('#0C1520'));c.rect(0,0,W,H,fill=1,stroke=0)
c.setFillColor(GOLD);c.setFont('UI-Bold',11);c.drawString(44,H-63,'SOLO CAMERA  /  CREATOR HANDBOOK')
c.setFillColor(colors.white);c.setFont('UI-Bold',43);c.drawString(42,H-135,'Your complete');c.drawString(42,H-185,'creator guide.')
c.setFont('UI',15);c.setFillColor(colors.HexColor('#BCCAD6'));c.drawString(44,H-223,'Capture. Demonstrate live. Edit. Export.')
# Real app screenshots, arranged as a restrained cover composition.
for key,x,y,width in [('02-camera',53,523,147),('21-editor',226,554,170),('46-live-screen',414,507,128)]:
    f=SCREENS/(key+'.png')
    with Image.open(f) as im:iw,ih=im.size
    h=width*ih/iw;c.setFillColor(colors.HexColor('#263543'));c.roundRect(x-5,y-h-5,width+10,h+10,11,fill=1,stroke=0);c.drawImage(str(f),x,y-h,width,h)
c.setFillColor(GOLD);c.rect(44,114,54,4,fill=1,stroke=0)
c.setFillColor(colors.white);c.setFont('UI-Bold',13);c.drawString(44,86,'ANDROID 0.52.1  |  NOVEMBER 2026')
c.setFont('UI',10);c.setFillColor(colors.HexColor('#BCCAD6'));c.drawString(44,64,'Real app screens, step-by-step workflows, reference and troubleshooting')
c.bookmarkPage('cover');c.addOutlineEntry('Cover','cover',level=0);c.showPage()

chrome(c,'How to use this guide',2)
c.setFillColor(INK);c.setFont('UI-Bold',28);c.drawString(42,H-91,'Start here.')
intro='''This manual documents **Solo Camera Android 0.52.1 (build 58)**. Screenshots were captured on a TECNO KM7k or generated from the app's actual title renderer. Earlier screenshots are retained where the controls are unchanged. Android dialogs, camera capabilities and available media can differ on another phone.

H|Choose your route

For your first recording, read the quick start. For a particular task, use the clickable contents or the PDF bookmarks. The **CONTENTS** link at the bottom of every page brings you back to the chapter list. You can also search the PDF for an on-screen label such as Split, Snap, Export audio or Background remover.

H|How instructions are written

**Camera > Shooting setup > Pre-recording check** means tap those controls in that order. Tap means one short touch; hold means keep your finger down until an action appears. A slider changes a value as you drag. In the editor, select the correct timeline object before opening its tools.

H|What the screenshots show

Camera images are examples. Editor screenshots use an isolated demonstration project with sample artwork and audio. Personal account data and live pairing secrets are excluded. A screenshot illustrates the interface; it does not guarantee that a particular lens, frame rate or optional feature works on every phone.

N|Read the current-build limits. Local editing, capture and optional supported tools work on the phone. Social account connection and automatic publishing are marked Coming soon in this build. A local publishing draft is not a scheduled social-media post. Large AI features listed as unavailable are not secretly installed or enabled.

H|Before a high-value shoot

Make a short test using your actual phone, microphone, receiver and lighting. Check the saved file and the sound. Keep source media and projects until you have verified your final export and made a separate backup.'''
draw_body(c,flowables(intro),42,H-125,W-84,75);c.bookmarkPage('start');c.addOutlineEntry('Using the manual','start',level=0);c.showPage()

# Clickable contents; each line links to the corresponding real page.
chunk_size=24
chunks=[pages[i:i+chunk_size] for i in range(0,len(pages),chunk_size)]
content_start=3+len(chunks)
offset=0
for toc_index,chunk in enumerate(chunks):
    chrome(c,'Contents',3+toc_index)
    if toc_index==0:c.bookmarkPage('contents');c.addOutlineEntry('Contents','contents',level=0)
    c.setFillColor(INK);c.setFont('UI-Bold',28);c.drawString(42,H-88,'Find your next step.' if toc_index==0 else 'Keep creating.')
    y=H-122;last=''
    for i,p in enumerate(chunk):
        idx=offset+i
        if p['section']!=last:
            y-=10;c.setFillColor(MUTED);c.setFont('UI-Bold',8);c.drawString(42,y,p['section'].upper());y-=13;last=p['section']
        c.setFillColor(INK);c.setFont('UI',9.5);title=p['title']
        if pdfmetrics.stringWidth(title,'UI',9.5)>440:raise ValueError('TOC title too long '+title)
        c.drawString(49,y,title);c.drawRightString(W-42,y,str(idx+content_start));c.linkRect('',f'page-{idx}',(42,y-4,W-40,y+12),relative=0,thickness=0);y-=18
    if y<70:raise ValueError('Contents overflow')
    c.showPage();offset+=len(chunk)

overflow=[];last_section=None
for i,p in enumerate(pages):
    chrome(c,p['section'],i+content_start);c.bookmarkPage(f'page-{i}')
    if p['section']!=last_section:c.addOutlineEntry(p['section'],f'page-{i}',level=0);last_section=p['section']
    c.addOutlineEntry(p['title'],f'page-{i}',level=1)
    title=Paragraph(markup(p['title']),ParagraphStyle('title',fontName='UI-Bold',fontSize=25,leading=29,textColor=INK))
    _,th=title.wrap(W-84,100);title.drawOn(c,42,H-76-th);top=H-96-th
    c.setFillColor(GOLD);c.rect(42,top+8,38,3,fill=1,stroke=0)
    width=293 if p['image'] else W-84
    # Fit at a readable minimum; never silently clip content.
    scale=1.0
    while scale>=.9:
        items=flowables(p['text'],scale)
        total=sum(item.wrap(width,1000)[1]+item.getSpaceBefore()+item.getSpaceAfter() for item in items)
        if total<=top-77:break
        scale-=.025
    if scale<.9:overflow.append((i+content_start,p['title'],round(total,1),round(top-77,1)))
    bottom=draw_body(c,items,42,top,width,68)
    if bottom<68:overflow.append((i+6,'DRAW '+p['title'],bottom))
    if p['image']:image_panel(c,p['image'],W-42-190,top-3,190,p['caption'])
    c.showPage()
c.save()
report={'pages':len(pages)+content_start-1,'chapters':len(pages),'screens_used':len(set(p['image'] for p in pages if p['image'])),'overflow':overflow,'file':str(OUT)}
(ROOT/'tmp/pdfs/manual-layout-report.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
if overflow:raise SystemExit('Layout needs repair: overflowing pages')

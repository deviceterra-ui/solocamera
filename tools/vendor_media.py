import urllib.request, xml.etree.ElementTree as ET, pathlib, re, json
ROOT=pathlib.Path(__file__).resolve().parents[1]/'vendor'/'maven'
BASES=['https://dl.google.com/dl/android/maven2/','https://repo.maven.apache.org/maven2/']
NS={'m':'http://maven.apache.org/POM/4.0.0'}
models={};seen=set();notices=[]
def fetch(rel,required=True):
 p=ROOT/rel
 if p.exists():return p.read_bytes()
 for base in BASES:
  try:
   data=urllib.request.urlopen(base+rel,timeout=25).read();p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data);return data
  except urllib.error.HTTPError as e:
   if e.code not in (404,403):raise
 if required:raise RuntimeError('Missing '+rel)
def sub(s,props):
 for _ in range(10):
  t=re.sub(r'\$\{([^}]+)\}',lambda m:props.get(m[1],m[0]),s or '')
  if t==s:return t
  s=t
 return s
def model(g,a,v):
 key=(g,a,v)
 if key in models:return models[key]
 rel=f'{g.replace(".","/")}/{a}/{v}/{a}-{v}.pom';root=ET.fromstring(fetch(rel));props={};managed={}
 parent=root.find('m:parent',NS)
 if parent is not None:
  _,props,managed=model(*[parent.findtext('m:'+x,namespaces=NS) for x in ('groupId','artifactId','version')]);props=props.copy();managed=managed.copy()
 props.update({'project.version':v,'pom.version':v,'project.groupId':g,'project.artifactId':a})
 for el in root.findall('m:properties/*',NS):props[el.tag.split('}')[-1]]=el.text or ''
 for d in root.findall('m:dependencyManagement/m:dependencies/m:dependency',NS):managed[(sub(d.findtext('m:groupId',namespaces=NS),props),sub(d.findtext('m:artifactId',namespaces=NS),props))]=sub(d.findtext('m:version',namespaces=NS),props)
 models[key]=(root,props,managed);return models[key]
def resolve(g,a,v):
 v=v.strip('[]');key=(g,a,v)
 if key in seen:return
 seen.add(key);root,props,managed=model(g,a,v);pack=root.findtext('m:packaging',default='jar',namespaces=NS)
 if pack=='bundle':pack='jar'
 if pack!='pom':
  fetch(f'{g.replace(".","/")}/{a}/{v}/{a}-{v}.{pack}');print(f'Vendored {g}:{a}:{v}',flush=True)
 notices.append({'artifact':':'.join(key),'licenses':[{'name':el.findtext('m:name',namespaces=NS),'url':el.findtext('m:url',namespaces=NS)} for el in root.findall('m:licenses/m:license',NS)]})
 for d in root.findall('m:dependencies/m:dependency',NS):
  if d.findtext('m:scope',namespaces=NS) in ('test','provided','system') or d.findtext('m:optional',namespaces=NS)=='true':continue
  dg=sub(d.findtext('m:groupId',namespaces=NS),props);da=sub(d.findtext('m:artifactId',namespaces=NS),props);dv=sub(d.findtext('m:version',namespaces=NS) or managed.get((dg,da)),props)
  if not dv or '${' in dv:raise RuntimeError(f'Unresolved {dg}:{da}:{dv}')
  resolve(dg,da,dv)
resolve('org.jetbrains.kotlin','kotlin-bom','1.8.0')
resolve('androidx.annotation','annotation-jvm','1.6.0')
for a in ['media3-transformer','media3-effect','media3-exoplayer','media3-ui']:resolve('androidx.media3',a,'1.8.0')
(ROOT.parent/'DEPENDENCIES.json').write_text(json.dumps(notices,indent=2),encoding='utf-8')
print('Dependency mirror ready')

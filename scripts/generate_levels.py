import random,json
from pathlib import Path
rng=random.Random(14215)
def solve(g,n,limit=2):
 out=[]
 def rec(cols,regions):
  r=len(cols)
  if r==n: out.append(cols);return
  for c in range(n):
   z=g[r*n+c]
   if c in cols or z in regions or (r and abs(c-cols[-1])<=1):continue
   rec(cols+[c],regions|{z})
   if len(out)>=limit:return
 rec([],set());return out
levels=[];seen=set()
for index in range(240):
 n=4 if index<20 else 5 if index<60 else 6 if index<120 else 7 if index<180 else 8
 while True:
  p=list(range(n));rng.shuffle(p)
  if any(abs(p[i]-p[i-1])<=1 for i in range(1,n)):continue
  g=[-1]*(n*n)
  for r,c in enumerate(p):g[r*n+c]=r
  remaining=n*n-n
  while remaining:
   edges=[]
   for i,z in enumerate(g):
    if z<0:continue
    r,c=divmod(i,n)
    for rr,cc in [(r-1,c),(r+1,c),(r,c-1),(r,c+1)]:
     if 0<=rr<n and 0<=cc<n and g[rr*n+cc]<0:edges.append((rr*n+cc,z))
   i,z=rng.choice(edges);g[i]=z;remaining-=1
  key=tuple(g)
  if key in seen:continue
  if len(solve(g,n))==1:
   seen.add(key);levels.append(dict(n=n,regions=g,solution=p));break
Path(__file__).resolve().parents[1].joinpath('app/src/main/assets/levels.js').write_text('const LEVELS = '+json.dumps(levels,separators=(',',':'))+';\nif(typeof module!=="undefined")module.exports=LEVELS;\n')
print('Generated',len(levels),'unique-solution connected puzzles')

(function(root){
const Engine={
 conflicts(level,cats,index){const n=level.n,r=Math.floor(index/n),c=index%n;return cats.filter(j=>j!==index&&(Math.floor(j/n)===r||j%n===c||level.regions[j]===level.regions[index]||(Math.abs(Math.floor(j/n)-r)<=1&&Math.abs(j%n-c)<=1)));},
 complete(level,cells){const cats=cells.flatMap((v,i)=>v===2?[i]:[]);return cats.length===level.n&&cats.every(i=>!this.conflicts(level,cats,i).length);},
 solve(level,limit=2){const out=[],n=level.n;function step(cols,used){let r=cols.length;if(r===n){out.push(cols);return;}for(let c=0;c<n;c++){const z=level.regions[r*n+c];if(cols.includes(c)||used.includes(z)||(r&&Math.abs(c-cols[r-1])<=1))continue;step([...cols,c],[...used,z]);if(out.length>=limit)return;}}step([],[]);return out;},
 fresh(level){return {cells:Array(level.n*level.n).fill(0),seconds:0,mistakes:0,won:false};}
};if(typeof module!=='undefined')module.exports=Engine;else root.Engine=Engine;
})(typeof window!=='undefined'?window:globalThis);

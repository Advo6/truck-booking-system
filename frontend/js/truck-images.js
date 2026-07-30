/** Resolve truck image paths — tries .jpg first (user photos), then .svg placeholders. */
const TruckImages = {
  paths: ['images/truck1.jpg', 'images/truck2.jpg'],

  /** Preferred load order per truck index */
  candidates(index, apiPath) {
    const i = index || 0;
    const list = [];
    if (apiPath && !apiPath.startsWith('http')) {
      list.push(apiPath);
      if (apiPath.endsWith('.jpg')) {
        list.push(apiPath.replace(/\.jpg$/i, '.svg'));
      }
    }
    list.push(`images/truck${i + 1}.jpg`);
    list.push(this.paths[i] || this.paths[0]);
    return [...new Set(list)];
  },

  src(index, apiPath) {
    return this.candidates(index, apiPath)[0];
  },

  onerrorAttr(index, apiPath) {
    const candidates = this.candidates(index, apiPath);
    const fallbacks = candidates.slice(1).map(p => `'${p}'`).join(',');
    return `this.onerror=(function(f){var i=0;return function(){if(i<f.length){this.src=f[i++];}else{this.onerror=null;}};})([${fallbacks}])`;
  }
};

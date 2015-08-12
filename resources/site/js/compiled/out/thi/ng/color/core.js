// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.color.core');
goog.require('cljs.core');
goog.require('thi.ng.math.core');
goog.require('thi.ng.strf.core');
thi.ng.color.core.THIRD = (1.0 / (3));
thi.ng.color.core.TWO_THIRD = (2.0 / (3));
thi.ng.color.core.SIXTH = (1.0 / (6));
thi.ng.color.core.INV8BIT = (1.0 / (255));
thi.ng.color.core.with_alpha = (function thi$ng$color$core$with_alpha(col,a){
if(cljs.core.truth_(a)){
return cljs.core.conj.call(null,col,a);
} else {
return col;
}
});
thi.ng.color.core.rgb__GT_hsv = (function thi$ng$color$core$rgb__GT_hsv(){
var G__31137 = arguments.length;
switch (G__31137) {
case 1:
return thi.ng.color.core.rgb__GT_hsv.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgb__GT_hsv.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgb__GT_hsv.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgb__GT_hsv.cljs$core$IFn$_invoke$arity$1 = (function (rgb){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hsv,rgb);
});

thi.ng.color.core.rgb__GT_hsv.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
var v = (function (){var x__16381__auto__ = (function (){var x__16381__auto__ = r;
var y__16382__auto__ = g;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})();
var y__16382__auto__ = b;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})();
var d = (v - (function (){var x__16388__auto__ = (function (){var x__16388__auto__ = r;
var y__16389__auto__ = g;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})();
var y__16389__auto__ = b;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})());
var s = (((v === (0)))?0.0:(d / v));
var h = (((s === (0)))?0.0:(function (){var pred__31138 = cljs.core._EQ__EQ_;
var expr__31139 = v;
if(cljs.core.truth_(pred__31138.call(null,r,expr__31139))){
return ((g - b) / d);
} else {
if(cljs.core.truth_(pred__31138.call(null,g,expr__31139))){
return (2.0 + ((b - r) / d));
} else {
return (4.0 + ((r - g) / d));
}
}
})());
var h__$1 = (h / 6.0);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(((h__$1 < (0)))?(h__$1 + (1)):h__$1),s,v], null);
});

thi.ng.color.core.rgb__GT_hsv.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return cljs.core.conj.call(null,thi.ng.color.core.rgb__GT_hsv.call(null,r,g,b),a);
});

thi.ng.color.core.rgb__GT_hsv.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.hsv__GT_rgb = (function thi$ng$color$core$hsv__GT_rgb(){
var G__31143 = arguments.length;
switch (G__31143) {
case 1:
return thi.ng.color.core.hsv__GT_rgb.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.hsv__GT_rgb.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.hsv__GT_rgb.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.hsv__GT_rgb.cljs$core$IFn$_invoke$arity$1 = (function (hsv){
return cljs.core.apply.call(null,thi.ng.color.core.hsv__GT_rgb,hsv);
});

thi.ng.color.core.hsv__GT_rgb.cljs$core$IFn$_invoke$arity$3 = (function (h,s,v){
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,0.0,s))){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [v,v,v], null);
} else {
var h__$1 = cljs.core.rem.call(null,(h * 6.0),6.0);
var i = (h__$1 | (0));
var f = (h__$1 - i);
var p = (v * (1.0 - s));
var q = (v * (1.0 - (s * f)));
var t = (v * (1.0 - ((1.0 - f) * s)));
var G__31144 = i;
switch (G__31144) {
case (0):
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [v,t,p], null);

break;
case (1):
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [q,v,p], null);

break;
case (2):
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [p,v,t], null);

break;
case (3):
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [p,q,v], null);

break;
case (4):
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [t,p,v], null);

break;
default:
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [v,p,q], null);

}
}
});

thi.ng.color.core.hsv__GT_rgb.cljs$core$IFn$_invoke$arity$4 = (function (h,s,v,a){
return cljs.core.conj.call(null,thi.ng.color.core.hsv__GT_rgb.call(null,h,s,v),a);
});

thi.ng.color.core.hsv__GT_rgb.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.rgb__GT_hsl = (function thi$ng$color$core$rgb__GT_hsl(){
var G__31148 = arguments.length;
switch (G__31148) {
case 1:
return thi.ng.color.core.rgb__GT_hsl.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgb__GT_hsl.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgb__GT_hsl.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgb__GT_hsl.cljs$core$IFn$_invoke$arity$1 = (function (rgb){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hsl,rgb);
});

thi.ng.color.core.rgb__GT_hsl.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
var f1 = (function (){var x__16388__auto__ = (function (){var x__16388__auto__ = r;
var y__16389__auto__ = g;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})();
var y__16389__auto__ = b;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})();
var f2 = (function (){var x__16381__auto__ = (function (){var x__16381__auto__ = r;
var y__16382__auto__ = g;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})();
var y__16382__auto__ = b;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})();
var l = ((f1 + f2) * 0.5);
var d = (f2 - f1);
if((d === (0))){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [0.0,0.0,l], null);
} else {
var s = (((l < 0.5))?(d / (f1 + f2)):(d / ((2.0 - f2) - f1)));
var d2 = (0.5 * d);
var dr = ((((f2 - r) * thi.ng.color.core.SIXTH) + d2) / d);
var dg = ((((f2 - g) * thi.ng.color.core.SIXTH) + d2) / d);
var db = ((((f2 - b) * thi.ng.color.core.SIXTH) + d2) / d);
var h = (function (){var pred__31149 = cljs.core._EQ__EQ_;
var expr__31150 = f2;
if(cljs.core.truth_(pred__31149.call(null,r,expr__31150))){
return (db - dg);
} else {
if(cljs.core.truth_(pred__31149.call(null,g,expr__31150))){
return ((thi.ng.color.core.THIRD + dr) - db);
} else {
return ((thi.ng.color.core.TWO_THIRD + dg) - dr);
}
}
})();
var h__$1 = (((h < (0)))?(h + (1)):(((h >= 1.0))?(h - (1)):h));
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [h__$1,s,l], null);
}
});

thi.ng.color.core.rgb__GT_hsl.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return cljs.core.conj.call(null,thi.ng.color.core.rgb__GT_hsl.call(null,r,g,b),a);
});

thi.ng.color.core.rgb__GT_hsl.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.hsl_hue__GT_rgb = (function thi$ng$color$core$hsl_hue__GT_rgb(f1,f2,h){
var h__$1 = (((h < (0)))?(h + (1)):(((h >= 1.0))?(h - (1)):h));
if((h__$1 < thi.ng.color.core.SIXTH)){
return thi.ng.math.core.mix.call(null,f1,f2,(6.0 * h__$1));
} else {
if((h__$1 < 0.5)){
return f2;
} else {
if((h__$1 < thi.ng.color.core.TWO_THIRD)){
return thi.ng.math.core.mix.call(null,f1,f2,((thi.ng.color.core.TWO_THIRD - h__$1) * 6.0));
} else {
return f1;

}
}
}
});
thi.ng.color.core.hsl__GT_rgb = (function thi$ng$color$core$hsl__GT_rgb(){
var G__31154 = arguments.length;
switch (G__31154) {
case 1:
return thi.ng.color.core.hsl__GT_rgb.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.hsl__GT_rgb.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.hsl__GT_rgb.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.hsl__GT_rgb.cljs$core$IFn$_invoke$arity$1 = (function (hsla){
return cljs.core.apply.call(null,thi.ng.color.core.hsl__GT_rgb,hsla);
});

thi.ng.color.core.hsl__GT_rgb.cljs$core$IFn$_invoke$arity$3 = (function (h,s,l){
if((s === (0))){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [l,l,l], null);
} else {
var f2 = (((l < 0.5))?(l * (s + (1))):((l + s) - (l * s)));
var f1 = ((2.0 * l) - f2);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.clamp.call(null,thi.ng.color.core.hsl_hue__GT_rgb.call(null,f1,f2,(h + thi.ng.color.core.THIRD)),0.0,1.0),thi.ng.math.core.clamp.call(null,thi.ng.color.core.hsl_hue__GT_rgb.call(null,f1,f2,h),0.0,1.0),thi.ng.math.core.clamp.call(null,thi.ng.color.core.hsl_hue__GT_rgb.call(null,f1,f2,(h - thi.ng.color.core.THIRD)),0.0,1.0)], null);
}
});

thi.ng.color.core.hsl__GT_rgb.cljs$core$IFn$_invoke$arity$4 = (function (h,s,l,a){
return cljs.core.conj.call(null,thi.ng.color.core.hsl__GT_rgb.call(null,h,s,l),a);
});

thi.ng.color.core.hsl__GT_rgb.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.hue__GT_rgb = (function thi$ng$color$core$hue__GT_rgb(h){
var h__$1 = (6.0 * h);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.clamp.call(null,(thi.ng.math.core.abs.call(null,(h__$1 - 3.0)) - (1)),0.0,1.0),thi.ng.math.core.clamp.call(null,(2.0 - thi.ng.math.core.abs.call(null,(h__$1 - 2.0))),0.0,1.0),thi.ng.math.core.clamp.call(null,(2.0 - thi.ng.math.core.abs.call(null,(h__$1 - 4.0))),0.0,1.0)], null);
});
thi.ng.color.core.rgb__GT_hcv = (function thi$ng$color$core$rgb__GT_hcv(){
var G__31157 = arguments.length;
switch (G__31157) {
case 1:
return thi.ng.color.core.rgb__GT_hcv.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgb__GT_hcv.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgb__GT_hcv.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgb__GT_hcv.cljs$core$IFn$_invoke$arity$1 = (function (rgba){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hcv,rgba);
});

thi.ng.color.core.rgb__GT_hcv.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
var vec__31158 = (((g < b))?new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [b,g,-1.0,thi.ng.color.core.TWO_THIRD], null):new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [g,b,0.0,(- thi.ng.color.core.THIRD)], null));
var px = cljs.core.nth.call(null,vec__31158,(0),null);
var py = cljs.core.nth.call(null,vec__31158,(1),null);
var pz = cljs.core.nth.call(null,vec__31158,(2),null);
var pw = cljs.core.nth.call(null,vec__31158,(3),null);
var vec__31159 = (((r < px))?new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [px,py,pw,r], null):new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [r,py,pz,px], null));
var qx = cljs.core.nth.call(null,vec__31159,(0),null);
var qy = cljs.core.nth.call(null,vec__31159,(1),null);
var qz = cljs.core.nth.call(null,vec__31159,(2),null);
var qw = cljs.core.nth.call(null,vec__31159,(3),null);
var c = (qx - (function (){var x__16388__auto__ = qw;
var y__16389__auto__ = qy;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})());
var h = thi.ng.math.core.abs.call(null,(((qw - qy) / ((6.0 * c) + 1.0E-10)) + qz));
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.clamp.call(null,h,0.0,1.0),thi.ng.math.core.clamp.call(null,c,0.0,1.0),thi.ng.math.core.clamp.call(null,qx,0.0,1.0)], null);
});

thi.ng.color.core.rgb__GT_hcv.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return cljs.core.conj.call(null,thi.ng.color.core.rgb__GT_hcv.call(null,r,g,b),a);
});

thi.ng.color.core.rgb__GT_hcv.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.rgb__GT_hcy = (function thi$ng$color$core$rgb__GT_hcy(){
var G__31162 = arguments.length;
switch (G__31162) {
case 1:
return thi.ng.color.core.rgb__GT_hcy.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgb__GT_hcy.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgb__GT_hcy.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgb__GT_hcy.cljs$core$IFn$_invoke$arity$1 = (function (rgba){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hcy,rgba);
});

thi.ng.color.core.rgb__GT_hcy.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
var vec__31163 = thi.ng.color.core.rgb__GT_hcv.call(null,r,g,b);
var h = cljs.core.nth.call(null,vec__31163,(0),null);
var c = cljs.core.nth.call(null,vec__31163,(1),null);
var v = cljs.core.nth.call(null,vec__31163,(2),null);
var y = (((0.299 * r) + (0.587 * g)) + (0.114 * b));
if((c === (0))){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [h,c,y], null);
} else {
var vec__31164 = thi.ng.color.core.hue__GT_rgb.call(null,h);
var r_SINGLEQUOTE_ = cljs.core.nth.call(null,vec__31164,(0),null);
var g_SINGLEQUOTE_ = cljs.core.nth.call(null,vec__31164,(1),null);
var b_SINGLEQUOTE_ = cljs.core.nth.call(null,vec__31164,(2),null);
var z = (((0.299 * r_SINGLEQUOTE_) + (0.587 * g_SINGLEQUOTE_)) + (0.114 * b_SINGLEQUOTE_));
if(((y - z) > 1.0E-5)){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [h,thi.ng.math.core.clamp.call(null,(c * ((1.0 - z) / (1.0 - y))),0.0,1.0),y], null);
} else {
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [h,thi.ng.math.core.clamp.call(null,(c * (z / y)),0.0,1.0),y], null);
}
}
});

thi.ng.color.core.rgb__GT_hcy.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return cljs.core.conj.call(null,thi.ng.color.core.rgb__GT_hcy.call(null,r,g,b),a);
});

thi.ng.color.core.rgb__GT_hcy.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.hcy__GT_rgb = (function thi$ng$color$core$hcy__GT_rgb(){
var G__31167 = arguments.length;
switch (G__31167) {
case 1:
return thi.ng.color.core.hcy__GT_rgb.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.hcy__GT_rgb.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.hcy__GT_rgb.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.hcy__GT_rgb.cljs$core$IFn$_invoke$arity$1 = (function (hcya){
return cljs.core.apply.call(null,thi.ng.color.core.hcy__GT_rgb,hcya);
});

thi.ng.color.core.hcy__GT_rgb.cljs$core$IFn$_invoke$arity$3 = (function (h,c,y){
var vec__31168 = thi.ng.color.core.hue__GT_rgb.call(null,h);
var r = cljs.core.nth.call(null,vec__31168,(0),null);
var g = cljs.core.nth.call(null,vec__31168,(1),null);
var b = cljs.core.nth.call(null,vec__31168,(2),null);
var z = (((0.299 * r) + (0.587 * g)) + (0.114 * b));
var c_SINGLEQUOTE_ = (((y < z))?(c * (y / z)):(((z < 1.0))?(c * ((1.0 - y) / (1.0 - z))):c));
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.clamp.call(null,(((r - z) * c_SINGLEQUOTE_) + y),0.0,1.0),thi.ng.math.core.clamp.call(null,(((g - z) * c_SINGLEQUOTE_) + y),0.0,1.0),thi.ng.math.core.clamp.call(null,(((b - z) * c_SINGLEQUOTE_) + y),0.0,1.0)], null);
});

thi.ng.color.core.hcy__GT_rgb.cljs$core$IFn$_invoke$arity$4 = (function (h,c,y,a){
return cljs.core.conj.call(null,thi.ng.color.core.hcy__GT_rgb.call(null,h,c,y),a);
});

thi.ng.color.core.hcy__GT_rgb.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.ycbcr__GT_rgb = (function thi$ng$color$core$ycbcr__GT_rgb(){
var G__31171 = arguments.length;
switch (G__31171) {
case 1:
return thi.ng.color.core.ycbcr__GT_rgb.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.ycbcr__GT_rgb.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.ycbcr__GT_rgb.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.ycbcr__GT_rgb.cljs$core$IFn$_invoke$arity$1 = (function (ycbcra){
return cljs.core.apply.call(null,thi.ng.color.core.ycbcr__GT_rgb,ycbcra);
});

thi.ng.color.core.ycbcr__GT_rgb.cljs$core$IFn$_invoke$arity$3 = (function (y,cb,cr){
var cb_SINGLEQUOTE_ = (cb - 0.5);
var cr_SINGLEQUOTE_ = (cr - 0.5);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.clamp.call(null,((cr_SINGLEQUOTE_ * 1.402) + y),0.0,1.0),thi.ng.math.core.clamp.call(null,(y - ((cb_SINGLEQUOTE_ * 0.34414) + (cr_SINGLEQUOTE_ * 0.71414))),0.0,1.0),thi.ng.math.core.clamp.call(null,((cb_SINGLEQUOTE_ * 1.772) + y),0.0,1.0)], null);
});

thi.ng.color.core.ycbcr__GT_rgb.cljs$core$IFn$_invoke$arity$4 = (function (y,cb,cr,a){
return cljs.core.conj.call(null,thi.ng.color.core.ycbcr__GT_rgb.call(null,y,cb,cr),a);
});

thi.ng.color.core.ycbcr__GT_rgb.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.rgb__GT_ycbcr = (function thi$ng$color$core$rgb__GT_ycbcr(){
var G__31174 = arguments.length;
switch (G__31174) {
case 1:
return thi.ng.color.core.rgb__GT_ycbcr.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgb__GT_ycbcr.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgb__GT_ycbcr.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgb__GT_ycbcr.cljs$core$IFn$_invoke$arity$1 = (function (rgba){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_ycbcr,rgba);
});

thi.ng.color.core.rgb__GT_ycbcr.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.clamp.call(null,(((0.299 * r) + (0.587 * g)) + (0.114 * b)),0.0,1.0),thi.ng.math.core.clamp.call(null,(((0.5 - (0.16874 * r)) - (0.33126 * g)) + (0.5 * b)),0.0,1.0),thi.ng.math.core.clamp.call(null,(((0.5 + (0.5 * r)) - (0.418688 * g)) - (0.081312 * b)),0.0,1.0)], null);
});

thi.ng.color.core.rgb__GT_ycbcr.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return cljs.core.conj.call(null,thi.ng.color.core.rgb__GT_ycbcr.call(null,r,g,b),a);
});

thi.ng.color.core.rgb__GT_ycbcr.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.rgb__GT_yuv = (function thi$ng$color$core$rgb__GT_yuv(){
var G__31177 = arguments.length;
switch (G__31177) {
case 1:
return thi.ng.color.core.rgb__GT_yuv.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgb__GT_yuv.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgb__GT_yuv.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgb__GT_yuv.cljs$core$IFn$_invoke$arity$1 = (function (rgba){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_yuv,rgba);
});

thi.ng.color.core.rgb__GT_yuv.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(((0.299 * r) + (0.587 * g)) + (0.114 * b)),(((-0.1473 * r) + (-0.28886 * g)) + (0.436 * b)),(((0.615 * r) + (-0.51499 * g)) + (-0.10001 * b))], null);
});

thi.ng.color.core.rgb__GT_yuv.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return cljs.core.conj.call(null,thi.ng.color.core.rgb__GT_yuv.call(null,r,g,b),a);
});

thi.ng.color.core.rgb__GT_yuv.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.yuv__GT_rgb = (function thi$ng$color$core$yuv__GT_rgb(){
var G__31180 = arguments.length;
switch (G__31180) {
case 1:
return thi.ng.color.core.yuv__GT_rgb.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.yuv__GT_rgb.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.yuv__GT_rgb.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.yuv__GT_rgb.cljs$core$IFn$_invoke$arity$1 = (function (yuva){
return cljs.core.apply.call(null,thi.ng.color.core.yuv__GT_rgb,yuva);
});

thi.ng.color.core.yuv__GT_rgb.cljs$core$IFn$_invoke$arity$3 = (function (y,u,v){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.clamp.call(null,((1.13983 * v) + y),0.0,1.0),thi.ng.math.core.clamp.call(null,(y - ((0.39465 * u) + (0.5806 * v))),0.0,1.0),thi.ng.math.core.clamp.call(null,((2.03211 * u) + y),0.0,1.0)], null);
});

thi.ng.color.core.yuv__GT_rgb.cljs$core$IFn$_invoke$arity$4 = (function (y,u,v,a){
return cljs.core.conj.call(null,thi.ng.color.core.yuv__GT_rgb.call(null,y,u,v),a);
});

thi.ng.color.core.yuv__GT_rgb.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.cmyk__GT_rgb = (function thi$ng$color$core$cmyk__GT_rgb(){
var G__31183 = arguments.length;
switch (G__31183) {
case 1:
return thi.ng.color.core.cmyk__GT_rgb.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 4:
return thi.ng.color.core.cmyk__GT_rgb.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
case 5:
return thi.ng.color.core.cmyk__GT_rgb.cljs$core$IFn$_invoke$arity$5((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.cmyk__GT_rgb.cljs$core$IFn$_invoke$arity$1 = (function (cmyk){
return cljs.core.apply.call(null,thi.ng.color.core.cmyk__GT_rgb,cmyk);
});

thi.ng.color.core.cmyk__GT_rgb.cljs$core$IFn$_invoke$arity$4 = (function (c,m,y,k){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1.0 - (function (){var x__16388__auto__ = 1.0;
var y__16389__auto__ = (c + k);
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})()),(1.0 - (function (){var x__16388__auto__ = 1.0;
var y__16389__auto__ = (m + k);
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})()),(1.0 - (function (){var x__16388__auto__ = 1.0;
var y__16389__auto__ = (y + k);
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})())], null);
});

thi.ng.color.core.cmyk__GT_rgb.cljs$core$IFn$_invoke$arity$5 = (function (c,m,y,k,a){
return cljs.core.conj.call(null,thi.ng.color.core.cmyk__GT_rgb.call(null,c,m,y,k),a);
});

thi.ng.color.core.cmyk__GT_rgb.cljs$lang$maxFixedArity = 5;
thi.ng.color.core.rgb__GT_cmyk = (function thi$ng$color$core$rgb__GT_cmyk(){
var G__31186 = arguments.length;
switch (G__31186) {
case 1:
return thi.ng.color.core.rgb__GT_cmyk.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgb__GT_cmyk.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgb__GT_cmyk.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgb__GT_cmyk.cljs$core$IFn$_invoke$arity$1 = (function (rgb){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_cmyk,rgb);
});

thi.ng.color.core.rgb__GT_cmyk.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
var c = (1.0 - r);
var m = (1.0 - g);
var y = (1.0 - b);
var k = (function (){var x__16388__auto__ = (function (){var x__16388__auto__ = c;
var y__16389__auto__ = m;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})();
var y__16389__auto__ = y;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})();
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [(function (){var x__16381__auto__ = (c - k);
var y__16382__auto__ = 0.0;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})(),(function (){var x__16381__auto__ = (m - k);
var y__16382__auto__ = 0.0;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})(),(function (){var x__16381__auto__ = (y - k);
var y__16382__auto__ = 0.0;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})(),(function (){var x__16381__auto__ = k;
var y__16382__auto__ = 0.0;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})()], null);
});

thi.ng.color.core.rgb__GT_cmyk.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return cljs.core.conj.call(null,thi.ng.color.core.rgb__GT_cmyk.call(null,r,g,b),a);
});

thi.ng.color.core.rgb__GT_cmyk.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.cie1931_gamma_correct = (function thi$ng$color$core$cie1931_gamma_correct(x){
return thi.ng.math.core.clamp.call(null,(((x < 0.0031308))?(12.92 * x):((1.055 * Math.pow(x,((1) / 2.4))) - 0.055)),0.0,1.0);
});
thi.ng.color.core.cie1931__GT_rgb = (function thi$ng$color$core$cie1931__GT_rgb(){
var G__31189 = arguments.length;
switch (G__31189) {
case 1:
return thi.ng.color.core.cie1931__GT_rgb.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.cie1931__GT_rgb.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.cie1931__GT_rgb.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.cie1931__GT_rgb.cljs$core$IFn$_invoke$arity$1 = (function (xyz){
return cljs.core.apply.call(null,thi.ng.color.core.cie1931__GT_rgb,xyz);
});

thi.ng.color.core.cie1931__GT_rgb.cljs$core$IFn$_invoke$arity$3 = (function (x,y,z){
return cljs.core.mapv.call(null,thi.ng.color.core.cie1931_gamma_correct,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(((3.2406 * x) + (-1.5372 * y)) + (-0.4986 * z)),(((-0.9689 * x) + (1.8758 * y)) + (0.0415 * z)),(((0.0557 * x) + (-0.204 * y)) + (1.057 * z))], null));
});

thi.ng.color.core.cie1931__GT_rgb.cljs$core$IFn$_invoke$arity$4 = (function (x,y,z,a){
return cljs.core.conj.call(null,thi.ng.color.core.cie1931__GT_rgb.call(null,x,y,z),a);
});

thi.ng.color.core.cie1931__GT_rgb.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.rgba__GT_int = (function thi$ng$color$core$rgba__GT_int(){
var G__31192 = arguments.length;
switch (G__31192) {
case 1:
return thi.ng.color.core.rgba__GT_int.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgba__GT_int.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgba__GT_int.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgba__GT_int.cljs$core$IFn$_invoke$arity$1 = (function (rgba){
return cljs.core.apply.call(null,thi.ng.color.core.rgba__GT_int,rgba);
});

thi.ng.color.core.rgba__GT_int.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
return (((((r * (255)) | (0)) << (16)) | (((g * (255)) | (0)) << (8))) | ((b * (255)) | (0)));
});

thi.ng.color.core.rgba__GT_int.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
return (thi.ng.color.core.rgba__GT_int.call(null,r,g,b) | (((a * (255)) | (0)) << (24)));
});

thi.ng.color.core.rgba__GT_int.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.int__GT_rgba = (function thi$ng$color$core$int__GT_rgba(int32){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [(thi.ng.color.core.INV8BIT * ((int32 >> (16)) & (255))),(thi.ng.color.core.INV8BIT * ((int32 >> (8)) & (255))),(thi.ng.color.core.INV8BIT * (int32 & (255))),(thi.ng.color.core.INV8BIT * ((int32 >>> (24)) & (255)))], null);
});
thi.ng.color.core.hex6 = new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, ["#",thi.ng.strf.core.hex.call(null,(6))], null);
thi.ng.color.core.int__GT_hex = (function thi$ng$color$core$int__GT_hex(i){
return thi.ng.strf.core.format.call(null,thi.ng.color.core.hex6,(i & (16777215)));
});
thi.ng.color.core.rgba__GT_css = (function thi$ng$color$core$rgba__GT_css(){
var G__31195 = arguments.length;
switch (G__31195) {
case 1:
return thi.ng.color.core.rgba__GT_css.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.rgba__GT_css.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.rgba__GT_css.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.rgba__GT_css.cljs$core$IFn$_invoke$arity$1 = (function (rgba){
return cljs.core.apply.call(null,thi.ng.color.core.rgba__GT_css,rgba);
});

thi.ng.color.core.rgba__GT_css.cljs$core$IFn$_invoke$arity$3 = (function (r,g,b){
return thi.ng.strf.core.format.call(null,thi.ng.color.core.hex6,thi.ng.color.core.rgba__GT_int.call(null,r,g,b));
});

thi.ng.color.core.rgba__GT_css.cljs$core$IFn$_invoke$arity$4 = (function (r,g,b,a){
if((a < 1.0)){
var r__$1 = ((255) * r);
var g__$1 = ((255) * g);
var b__$1 = ((255) * b);
return [cljs.core.str("rgba("),cljs.core.str((r__$1 | (0))),cljs.core.str(","),cljs.core.str((g__$1 | (0))),cljs.core.str(","),cljs.core.str((b__$1 | (0))),cljs.core.str(","),cljs.core.str((function (){var x__16381__auto__ = 0.0;
var y__16382__auto__ = a;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})()),cljs.core.str(")")].join('');
} else {
return thi.ng.strf.core.format.call(null,thi.ng.color.core.hex6,thi.ng.color.core.rgba__GT_int.call(null,r,g,b));
}
});

thi.ng.color.core.rgba__GT_css.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.hsva__GT_css = (function thi$ng$color$core$hsva__GT_css(){
var G__31198 = arguments.length;
switch (G__31198) {
case 1:
return thi.ng.color.core.hsva__GT_css.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.hsva__GT_css.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.hsva__GT_css.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.hsva__GT_css.cljs$core$IFn$_invoke$arity$1 = (function (hsva){
return cljs.core.apply.call(null,thi.ng.color.core.hsva__GT_css,hsva);
});

thi.ng.color.core.hsva__GT_css.cljs$core$IFn$_invoke$arity$3 = (function (h,s,v){
return cljs.core.apply.call(null,thi.ng.color.core.rgba__GT_css,thi.ng.color.core.hsv__GT_rgb.call(null,h,s,v));
});

thi.ng.color.core.hsva__GT_css.cljs$core$IFn$_invoke$arity$4 = (function (h,s,v,a){
return cljs.core.apply.call(null,thi.ng.color.core.rgba__GT_css,cljs.core.conj.call(null,thi.ng.color.core.hsv__GT_rgb.call(null,h,s,v),a));
});

thi.ng.color.core.hsva__GT_css.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.hsla__GT_css = (function thi$ng$color$core$hsla__GT_css(){
var G__31201 = arguments.length;
switch (G__31201) {
case 1:
return thi.ng.color.core.hsla__GT_css.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 3:
return thi.ng.color.core.hsla__GT_css.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return thi.ng.color.core.hsla__GT_css.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.hsla__GT_css.cljs$core$IFn$_invoke$arity$1 = (function (hsla){
return cljs.core.apply.call(null,thi.ng.color.core.hsla__GT_css,hsla);
});

thi.ng.color.core.hsla__GT_css.cljs$core$IFn$_invoke$arity$3 = (function (h,s,l){
var h__$1 = ((h * (360)) | (0));
var s__$1 = ((s * (100)) | (0));
var l__$1 = ((l * (100)) | (0));
return [cljs.core.str("hsl("),cljs.core.str(h__$1),cljs.core.str(","),cljs.core.str(s__$1),cljs.core.str(","),cljs.core.str(l__$1),cljs.core.str(")")].join('');
});

thi.ng.color.core.hsla__GT_css.cljs$core$IFn$_invoke$arity$4 = (function (h,s,l,a){
var h__$1 = ((h * (360)) | (0));
var s__$1 = ((s * (100)) | (0));
var l__$1 = ((l * (100)) | (0));
return [cljs.core.str("hsla("),cljs.core.str(h__$1),cljs.core.str(","),cljs.core.str(s__$1),cljs.core.str("%,"),cljs.core.str(l__$1),cljs.core.str("%,"),cljs.core.str(a),cljs.core.str(")")].join('');
});

thi.ng.color.core.hsla__GT_css.cljs$lang$maxFixedArity = 4;
thi.ng.color.core.hex__GT_rgba = (function thi$ng$color$core$hex__GT_rgba(hex){
var hex__$1 = ((cljs.core._EQ_.call(null,"#",cljs.core.first.call(null,hex)))?cljs.core.subs.call(null,hex,(1)):hex);
var rgba = ((((3) === cljs.core.count.call(null,hex__$1)))?(function (){var vec__31204 = hex__$1;
var r = cljs.core.nth.call(null,vec__31204,(0),null);
var g = cljs.core.nth.call(null,vec__31204,(1),null);
var b = cljs.core.nth.call(null,vec__31204,(2),null);
return thi.ng.color.core.int__GT_rgba.call(null,thi.ng.strf.core.parse_int.call(null,[cljs.core.str(r),cljs.core.str(r),cljs.core.str(g),cljs.core.str(g),cljs.core.str(b),cljs.core.str(b)].join(''),(16),(0)));
})():thi.ng.color.core.int__GT_rgba.call(null,thi.ng.strf.core.parse_int.call(null,hex__$1,(16),(0))));
if(cljs.core.truth_((function (){var and__16057__auto__ = rgba;
if(cljs.core.truth_(and__16057__auto__)){
return (cljs.core.count.call(null,hex__$1) < (7));
} else {
return and__16057__auto__;
}
})())){
return cljs.core.assoc.call(null,rgba,(3),1.0);
} else {
return rgba;
}
});
thi.ng.color.core.parse_channel_val = (function thi$ng$color$core$parse_channel_val(c){
if((c.indexOf("%") > (0))){
return (0.01 * thi.ng.strf.core.parse_float.call(null,cljs.core.subs.call(null,c,(0),(cljs.core.count.call(null,c) - (1)))));
} else {
return (thi.ng.color.core.INV8BIT * thi.ng.strf.core.parse_int.call(null,c,(10),(0)));
}
});
thi.ng.color.core.css__GT_rgba = (function thi$ng$color$core$css__GT_rgba(css){
if(cljs.core._EQ_.call(null,"#",cljs.core.first.call(null,css))){
return thi.ng.color.core.hex__GT_rgba.call(null,css);
} else {
var vec__31207 = cljs.core.re_seq.call(null,/(rgb|hsl)a?\((\d+%?),(\d+%?),(\d+%?),?([0-9\.]+)?\)/,css);
var vec__31208 = cljs.core.nth.call(null,vec__31207,(0),null);
var _ = cljs.core.nth.call(null,vec__31208,(0),null);
var mode = cljs.core.nth.call(null,vec__31208,(1),null);
var a = cljs.core.nth.call(null,vec__31208,(2),null);
var b = cljs.core.nth.call(null,vec__31208,(3),null);
var c = cljs.core.nth.call(null,vec__31208,(4),null);
var d = cljs.core.nth.call(null,vec__31208,(5),null);
var col = vec__31208;
if(cljs.core.truth_(mode)){
if(cljs.core.truth_(new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["rgba",null,"rgb",null], null), null).call(null,mode))){
return cljs.core.conj.call(null,cljs.core.mapv.call(null,thi.ng.color.core.parse_channel_val,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [a,b,c], null)),thi.ng.strf.core.parse_float.call(null,d,1.0));
} else {
var h = (thi.ng.strf.core.parse_float.call(null,a) / 360.0);
var s = thi.ng.color.core.parse_channel_val.call(null,b);
var l = thi.ng.color.core.parse_channel_val.call(null,c);
return cljs.core.conj.call(null,thi.ng.color.core.hsl__GT_rgb.call(null,h,s,l),thi.ng.strf.core.parse_float.call(null,d,1.0));
}
} else {
return null;
}
}
});
thi.ng.color.core.red = cljs.core.first;
thi.ng.color.core.green = (function thi$ng$color$core$green(p1__31209_SHARP_){
return cljs.core.nth.call(null,p1__31209_SHARP_,(1));
});
thi.ng.color.core.blue = (function thi$ng$color$core$blue(p1__31210_SHARP_){
return cljs.core.nth.call(null,p1__31210_SHARP_,(2));
});
thi.ng.color.core.hue = cljs.core.first;
thi.ng.color.core.saturation = (function thi$ng$color$core$saturation(p1__31211_SHARP_){
return cljs.core.nth.call(null,p1__31211_SHARP_,(1));
});
thi.ng.color.core.brightness = (function thi$ng$color$core$brightness(p1__31212_SHARP_){
return cljs.core.nth.call(null,p1__31212_SHARP_,(2));
});
thi.ng.color.core.lightness = (function thi$ng$color$core$lightness(p1__31213_SHARP_){
return cljs.core.nth.call(null,p1__31213_SHARP_,(2));
});
thi.ng.color.core.cyan = cljs.core.first;
thi.ng.color.core.magenta = (function thi$ng$color$core$magenta(p1__31214_SHARP_){
return cljs.core.nth.call(null,p1__31214_SHARP_,(1));
});
thi.ng.color.core.yellow = (function thi$ng$color$core$yellow(p1__31215_SHARP_){
return cljs.core.nth.call(null,p1__31215_SHARP_,(2));
});
thi.ng.color.core.black = (function thi$ng$color$core$black(p1__31216_SHARP_){
return cljs.core.nth.call(null,p1__31216_SHARP_,(3));
});
thi.ng.color.core.alpha = (function thi$ng$color$core$alpha(p1__31217_SHARP_){
return cljs.core.nth.call(null,p1__31217_SHARP_,((((4) === cljs.core.count.call(null,p1__31217_SHARP_)))?(3):(4)),(1));
});
thi.ng.color.core.hues = cljs.core.zipmap.call(null,new cljs.core.PersistentVector(null, 11, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"red","red",-969428204),new cljs.core.Keyword(null,"orange","orange",73816386),new cljs.core.Keyword(null,"yellow","yellow",-881035449),new cljs.core.Keyword(null,"lime","lime",-1796425088),new cljs.core.Keyword(null,"green","green",-945526839),new cljs.core.Keyword(null,"teal","teal",1231496088),new cljs.core.Keyword(null,"cyan","cyan",1118839274),new cljs.core.Keyword(null,"azure","azure",1864287702),new cljs.core.Keyword(null,"blue","blue",-622100620),new cljs.core.Keyword(null,"purple","purple",-876021126),new cljs.core.Keyword(null,"magenta","magenta",1687937081)], null),cljs.core.map.call(null,(function (p1__31218_SHARP_){
return (p1__31218_SHARP_ / 360.0);
}),cljs.core.range.call(null,(0),(360),(30))));
thi.ng.color.core.primary_hues = cljs.core.select_keys.call(null,thi.ng.color.core.hues,new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"red","red",-969428204),new cljs.core.Keyword(null,"yellow","yellow",-881035449),new cljs.core.Keyword(null,"green","green",-945526839),new cljs.core.Keyword(null,"cyan","cyan",1118839274),new cljs.core.Keyword(null,"blue","blue",-622100620),new cljs.core.Keyword(null,"magenta","magenta",1687937081)], null));
thi.ng.color.core.closest_hue = (function thi$ng$color$core$closest_hue(){
var G__31220 = arguments.length;
switch (G__31220) {
case 1:
return thi.ng.color.core.closest_hue.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return thi.ng.color.core.closest_hue.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.color.core.closest_hue.cljs$core$IFn$_invoke$arity$1 = (function (h){
return thi.ng.color.core.closest_hue.call(null,h,thi.ng.color.core.hues);
});

thi.ng.color.core.closest_hue.cljs$core$IFn$_invoke$arity$2 = (function (h,hues){
return cljs.core.first.call(null,cljs.core.reduce.call(null,(function (p__31221,p__31222){
var vec__31223 = p__31221;
var h_SINGLEQUOTE_ = cljs.core.nth.call(null,vec__31223,(0),null);
var d_SINGLEQUOTE_ = cljs.core.nth.call(null,vec__31223,(1),null);
var vec__31224 = p__31222;
var k = cljs.core.nth.call(null,vec__31224,(0),null);
var v = cljs.core.nth.call(null,vec__31224,(1),null);
var d = (function (){var x__16388__auto__ = thi.ng.math.core.abs_diff.call(null,h,v);
var y__16389__auto__ = thi.ng.math.core.abs_diff.call(null,(h - (1)),v);
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})();
if((d < d_SINGLEQUOTE_)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [k,d], null);
} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [h_SINGLEQUOTE_,d_SINGLEQUOTE_], null);
}
}),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [null,1000000.0], null),hues));
});

thi.ng.color.core.closest_hue.cljs$lang$maxFixedArity = 2;
thi.ng.color.core.hue_rgb = (function thi$ng$color$core$hue_rgb(rgb){
return cljs.core.first.call(null,cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hsv,rgb));
});
thi.ng.color.core.saturation_rgb = (function thi$ng$color$core$saturation_rgb(p__31226){
var vec__31228 = p__31226;
var r = cljs.core.nth.call(null,vec__31228,(0),null);
var g = cljs.core.nth.call(null,vec__31228,(1),null);
var b = cljs.core.nth.call(null,vec__31228,(2),null);
var v = (function (){var x__16381__auto__ = (function (){var x__16381__auto__ = r;
var y__16382__auto__ = g;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})();
var y__16382__auto__ = b;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})();
var d = (v - (function (){var x__16388__auto__ = (function (){var x__16388__auto__ = r;
var y__16389__auto__ = g;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})();
var y__16389__auto__ = b;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})());
if((v === (0))){
return 0.0;
} else {
return (d / v);
}
});
thi.ng.color.core.brightness_rgb = (function thi$ng$color$core$brightness_rgb(p__31229){
var vec__31231 = p__31229;
var r = cljs.core.nth.call(null,vec__31231,(0),null);
var g = cljs.core.nth.call(null,vec__31231,(1),null);
var b = cljs.core.nth.call(null,vec__31231,(2),null);
var x__16381__auto__ = (function (){var x__16381__auto__ = r;
var y__16382__auto__ = g;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})();
var y__16382__auto__ = b;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
});
thi.ng.color.core.luminance_rgb = (function thi$ng$color$core$luminance_rgb(p__31232){
var vec__31234 = p__31232;
var r = cljs.core.nth.call(null,vec__31234,(0),null);
var g = cljs.core.nth.call(null,vec__31234,(1),null);
var b = cljs.core.nth.call(null,vec__31234,(2),null);
return (((0.299 * r) + (0.587 * g)) + (0.114 * b));
});
/**
 * Returns new HSV color with its hue rotated by theta (in radians).
 */
thi.ng.color.core.rotate_hue_hsv = (function thi$ng$color$core$rotate_hue_hsv(p__31235,theta){
var vec__31237 = p__31235;
var h = cljs.core.nth.call(null,vec__31237,(0),null);
var s = cljs.core.nth.call(null,vec__31237,(1),null);
var v = cljs.core.nth.call(null,vec__31237,(2),null);
var h__$1 = (h + (cljs.core.rem.call(null,theta,thi.ng.math.core.TWO_PI) / thi.ng.math.core.TWO_PI));
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(((h__$1 < (0)))?(h__$1 + (1)):(((h__$1 >= 1.0))?(h__$1 - (1)):h__$1
)),s,v], null);
});
thi.ng.color.core.adjust_saturation_hsv = (function thi$ng$color$core$adjust_saturation_hsv(hsv,x){
return cljs.core.update_in.call(null,hsv,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1)], null),(function (p1__31238_SHARP_){
return thi.ng.math.core.clamp.call(null,(x + p1__31238_SHARP_),0.0,1.0);
}));
});
thi.ng.color.core.adjust_brightness_hsv = (function thi$ng$color$core$adjust_brightness_hsv(hsv,x){
return cljs.core.update_in.call(null,hsv,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [(2)], null),(function (p1__31239_SHARP_){
return thi.ng.math.core.clamp.call(null,(x + p1__31239_SHARP_),0.0,1.0);
}));
});
thi.ng.color.core.rotate_hue_rgb = (function thi$ng$color$core$rotate_hue_rgb(rgb,theta){
return cljs.core.apply.call(null,thi.ng.color.core.hsv__GT_rgb,thi.ng.color.core.rotate_hue_hsv.call(null,cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hsv,rgb),theta));
});
thi.ng.color.core.adjust_saturation_rgb = (function thi$ng$color$core$adjust_saturation_rgb(rgb,x){
var vec__31241 = cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hsv,rgb);
var h = cljs.core.nth.call(null,vec__31241,(0),null);
var s = cljs.core.nth.call(null,vec__31241,(1),null);
var v = cljs.core.nth.call(null,vec__31241,(2),null);
return thi.ng.color.core.hsv__GT_rgb.call(null,h,thi.ng.math.core.clamp.call(null,(x + s),0.0,1.0),v);
});
thi.ng.color.core.adjust_brightness_rgb = (function thi$ng$color$core$adjust_brightness_rgb(rgb,x){
var vec__31243 = cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hsv,rgb);
var h = cljs.core.nth.call(null,vec__31243,(0),null);
var s = cljs.core.nth.call(null,vec__31243,(1),null);
var v = cljs.core.nth.call(null,vec__31243,(2),null);
return thi.ng.color.core.hsv__GT_rgb.call(null,h,s,thi.ng.math.core.clamp.call(null,(x + v),0.0,1.0));
});
thi.ng.color.core.adjust_alpha = (function thi$ng$color$core$adjust_alpha(col,x){
return cljs.core.update_in.call(null,col,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [(3)], null),(function (p1__31244_SHARP_){
return thi.ng.math.core.clamp.call(null,(x + (function (){var or__16069__auto__ = p1__31244_SHARP_;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return (1);
}
})()),0.0,1.0);
}));
});
thi.ng.color.core.gamma_rgba = (function thi$ng$color$core$gamma_rgba(p__31246,e){
var vec__31249 = p__31246;
var r = cljs.core.nth.call(null,vec__31249,(0),null);
var g = cljs.core.nth.call(null,vec__31249,(1),null);
var b = cljs.core.nth.call(null,vec__31249,(2),null);
var vec__31250 = cljs.core.nthnext.call(null,vec__31249,(3));
var a = cljs.core.nth.call(null,vec__31250,(0),null);
var rgb_SINGLEQUOTE_ = cljs.core.mapv.call(null,((function (vec__31249,r,g,b,vec__31250,a){
return (function (p1__31245_SHARP_){
return Math.pow(p1__31245_SHARP_,e);
});})(vec__31249,r,g,b,vec__31250,a))
,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [r,g,b], null));
if(cljs.core.truth_(a)){
return cljs.core.conj.call(null,rgb_SINGLEQUOTE_,a);
} else {
return rgb_SINGLEQUOTE_;
}
});
/**
 * Returns new HSV color with its hue rotated by 180 degrees.
 */
thi.ng.color.core.complementary_hsv = (function thi$ng$color$core$complementary_hsv(hsv){
return thi.ng.color.core.rotate_hue_hsv.call(null,hsv,thi.ng.math.core.PI);
});
thi.ng.color.core.complementary_rgb = (function thi$ng$color$core$complementary_rgb(rgb){
return thi.ng.color.core.rotate_hue_rgb.call(null,rgb,thi.ng.math.core.PI);
});
thi.ng.color.core.invert_rgb = (function thi$ng$color$core$invert_rgb(p__31251){
var vec__31253 = p__31251;
var r = cljs.core.nth.call(null,vec__31253,(0),null);
var g = cljs.core.nth.call(null,vec__31253,(1),null);
var b = cljs.core.nth.call(null,vec__31253,(2),null);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1.0 - r),(1.0 - g),(1.0 - b)], null);
});
thi.ng.color.core.invert_hsv = (function thi$ng$color$core$invert_hsv(hsv){
return cljs.core.apply.call(null,thi.ng.color.core.rgb__GT_hsv,thi.ng.color.core.invert_rgb.call(null,cljs.core.apply.call(null,thi.ng.color.core.hsv__GT_rgb,hsv)));
});
thi.ng.color.core.dist_rgb = (function thi$ng$color$core$dist_rgb(rgb1,rgb2){
var dr = (cljs.core.first.call(null,rgb1) - cljs.core.first.call(null,rgb2));
var dg = (cljs.core.nth.call(null,rgb1,(1)) - cljs.core.nth.call(null,rgb2,(1)));
var db = (cljs.core.nth.call(null,rgb1,(2)) - cljs.core.nth.call(null,rgb2,(2)));
return Math.sqrt((((dr * dr) + (dg * dg)) + (db * db)));
});
thi.ng.color.core.dist_hsv = (function thi$ng$color$core$dist_hsv(p__31254,p__31255){
var vec__31258 = p__31254;
var ha = cljs.core.nth.call(null,vec__31258,(0),null);
var sa = cljs.core.nth.call(null,vec__31258,(1),null);
var va = cljs.core.nth.call(null,vec__31258,(2),null);
var vec__31259 = p__31255;
var hb = cljs.core.nth.call(null,vec__31259,(0),null);
var sb = cljs.core.nth.call(null,vec__31259,(1),null);
var vb = cljs.core.nth.call(null,vec__31259,(2),null);
var ha__$1 = (thi.ng.math.core.TWO_PI * ha);
var hb__$1 = (thi.ng.math.core.TWO_PI * hb);
var dh = ((sa * Math.cos(ha__$1)) - (sb * Math.cos(hb__$1)));
var ds = ((sa * Math.sin(ha__$1)) - (sb * Math.sin(hb__$1)));
var dv = (va - vb);
return Math.sqrt((((dh * dh) + (ds * ds)) + (dv * dv)));
});
thi.ng.color.core.blend_rgb = (function thi$ng$color$core$blend_rgb(rgb1,rgb2,t){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(function (){var a__18448__auto__ = cljs.core.first.call(null,rgb1);
return (((cljs.core.first.call(null,rgb2) - a__18448__auto__) * t) + a__18448__auto__);
})(),(function (){var a__18448__auto__ = cljs.core.nth.call(null,rgb1,(1));
return (((cljs.core.nth.call(null,rgb2,(1)) - a__18448__auto__) * t) + a__18448__auto__);
})(),(function (){var a__18448__auto__ = cljs.core.nth.call(null,rgb1,(2));
return (((cljs.core.nth.call(null,rgb2,(2)) - a__18448__auto__) * t) + a__18448__auto__);
})()], null);
});
thi.ng.color.core.blend_hsv = (function thi$ng$color$core$blend_hsv(hsv1,hsv2,t){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(function (){var h1 = cljs.core.first.call(null,hsv1);
var h2 = cljs.core.first.call(null,hsv2);
var hd = thi.ng.math.core.abs_diff.call(null,h1,h2);
if((hd > 0.5)){
if((h2 > h1)){
return cljs.core.rem.call(null,(function (){var a__18448__auto__ = (h1 + (1));
return (((h2 - a__18448__auto__) * t) + a__18448__auto__);
})(),1.0);
} else {
return cljs.core.rem.call(null,(function (){var a__18448__auto__ = h1;
return ((((h2 + (1)) - a__18448__auto__) * t) + a__18448__auto__);
})(),1.0);
}
} else {
var a__18448__auto__ = h1;
return (((h2 - a__18448__auto__) * t) + a__18448__auto__);
}
})(),(function (){var a__18448__auto__ = cljs.core.nth.call(null,hsv1,(1));
return (((cljs.core.nth.call(null,hsv2,(1)) - a__18448__auto__) * t) + a__18448__auto__);
})(),(function (){var a__18448__auto__ = cljs.core.nth.call(null,hsv1,(2));
return (((cljs.core.nth.call(null,hsv2,(2)) - a__18448__auto__) * t) + a__18448__auto__);
})()], null);
});
thi.ng.color.core.blend_rgba = (function thi$ng$color$core$blend_rgba(p__31260,p__31261){
var vec__31264 = p__31260;
var dr = cljs.core.nth.call(null,vec__31264,(0),null);
var dg = cljs.core.nth.call(null,vec__31264,(1),null);
var db = cljs.core.nth.call(null,vec__31264,(2),null);
var da = cljs.core.nth.call(null,vec__31264,(3),null);
var vec__31265 = p__31261;
var sr = cljs.core.nth.call(null,vec__31265,(0),null);
var sg = cljs.core.nth.call(null,vec__31265,(1),null);
var sb = cljs.core.nth.call(null,vec__31265,(2),null);
var sa = cljs.core.nth.call(null,vec__31265,(3),null);
var sa__$1 = (function (){var or__16069__auto__ = sa;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return 1.0;
}
})();
var da__$1 = (function (){var or__16069__auto__ = da;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return 1.0;
}
})();
var da_SINGLEQUOTE_ = ((1.0 - sa__$1) * da__$1);
var a_SINGLEQUOTE_ = (sa__$1 + da_SINGLEQUOTE_);
var ia_SINGLEQUOTE_ = (1.0 / a_SINGLEQUOTE_);
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [(ia_SINGLEQUOTE_ * ((sr * sa__$1) + (dr * da_SINGLEQUOTE_))),(ia_SINGLEQUOTE_ * ((sg * sa__$1) + (dg * da_SINGLEQUOTE_))),(ia_SINGLEQUOTE_ * ((sb * sa__$1) + (db * da_SINGLEQUOTE_))),a_SINGLEQUOTE_], null);
});

//# sourceMappingURL=core.js.map?rel=1439206035842
// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.geom.svg.core');
goog.require('cljs.core');
goog.require('thi.ng.geom.core.matrix');
goog.require('thi.ng.geom.core.utils');
goog.require('thi.ng.color.core');
goog.require('thi.ng.geom.core');
goog.require('thi.ng.geom.core.vector');
goog.require('thi.ng.math.core');
goog.require('thi.ng.dstruct.core');
goog.require('thi.ng.strf.core');
thi.ng.geom.svg.core.stroke_round = new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"stroke-linecap","stroke-linecap",-1201103248),"round",new cljs.core.Keyword(null,"stroke-linejoin","stroke-linejoin",-1810816406),"round"], null);
thi.ng.geom.svg.core.xml_preamble = "<?xml version=\"1.0\"?>\n";
thi.ng.geom.svg.core._STAR_ff_STAR_ = thi.ng.strf.core.float$.call(null,(2));
thi.ng.geom.svg.core._STAR_fmt_vec_STAR_ = (function thi$ng$geom$svg$core$_STAR_fmt_vec_STAR_(p){
return [cljs.core.str(thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,cljs.core.first.call(null,p))),cljs.core.str(","),cljs.core.str(thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,cljs.core.nth.call(null,p,(1))))].join('');
});
thi.ng.geom.svg.core._STAR_fmt_percent_STAR_ = (function thi$ng$geom$svg$core$_STAR_fmt_percent_STAR_(x){
return [cljs.core.str(((x * (100)) | (0))),cljs.core.str("%")].join('');
});
thi.ng.geom.svg.core._STAR_fmt_matrix_STAR_ = new cljs.core.PersistentVector(null, 13, 5, cljs.core.PersistentVector.EMPTY_NODE, ["matrix(",thi.ng.geom.svg.core._STAR_ff_STAR_,",",thi.ng.geom.svg.core._STAR_ff_STAR_,",",thi.ng.geom.svg.core._STAR_ff_STAR_,",",thi.ng.geom.svg.core._STAR_ff_STAR_,",",thi.ng.geom.svg.core._STAR_ff_STAR_,",",thi.ng.geom.svg.core._STAR_ff_STAR_,")"], null);
thi.ng.geom.svg.core.point_seq_format2 = new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_], null);
thi.ng.geom.svg.core.point_seq_format3 = new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_], null);
thi.ng.geom.svg.core.point_seq_format4 = new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_], null);
thi.ng.geom.svg.core.point_seq_format = (function thi$ng$geom$svg$core$point_seq_format(n){
var G__30818 = (n | (0));
switch (G__30818) {
case (1):
return new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.svg.core._STAR_fmt_vec_STAR_], null);

break;
case (2):
return thi.ng.geom.svg.core.point_seq_format2;

break;
case (3):
return thi.ng.geom.svg.core.point_seq_format3;

break;
case (4):
return thi.ng.geom.svg.core.point_seq_format4;

break;
default:
return cljs.core.interpose.call(null," ",cljs.core.repeat.call(null,n,thi.ng.geom.svg.core._STAR_fmt_vec_STAR_));

}
});
thi.ng.geom.svg.core.path_segment_formats = cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"L","L",-1038307519),new cljs.core.Keyword(null,"M","M",-1755742206),new cljs.core.Keyword(null,"A","A",-1688942394),new cljs.core.Keyword(null,"m","m",1632677161),new cljs.core.Keyword(null,"Z","Z",459124588),new cljs.core.Keyword(null,"C","C",-173629587),new cljs.core.Keyword(null,"l","l",1395893423),new cljs.core.Keyword(null,"z","z",-789527183),new cljs.core.Keyword(null,"c","c",-1763192079),new cljs.core.Keyword(null,"a","a",-2123407586)],[new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, ["L",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, ["M",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null),new cljs.core.PersistentVector(null, 11, 5, cljs.core.PersistentVector.EMPTY_NODE, ["A",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_ff_STAR_," ",cljs.core.str," ",cljs.core.str," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, ["m",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, ["Z"], null),new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, ["C",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, ["l",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, ["z"], null),new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, ["c",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null),new cljs.core.PersistentVector(null, 11, 5, cljs.core.PersistentVector.EMPTY_NODE, ["a",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," ",thi.ng.geom.svg.core._STAR_ff_STAR_," ",cljs.core.str," ",cljs.core.str," ",thi.ng.geom.svg.core._STAR_fmt_vec_STAR_," "], null)]);

thi.ng.geom.svg.core.PSVGConvert = (function (){var obj30821 = {};
return obj30821;
})();

thi.ng.geom.svg.core.as_svg = (function thi$ng$geom$svg$core$as_svg(_,opts){
if((function (){var and__16057__auto__ = _;
if(and__16057__auto__){
return _.thi$ng$geom$svg$core$PSVGConvert$as_svg$arity$2;
} else {
return and__16057__auto__;
}
})()){
return _.thi$ng$geom$svg$core$PSVGConvert$as_svg$arity$2(_,opts);
} else {
var x__16705__auto__ = (((_ == null))?null:_);
return (function (){var or__16069__auto__ = (thi.ng.geom.svg.core.as_svg[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (thi.ng.geom.svg.core.as_svg["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"PSVGConvert.as-svg",_);
}
}
})().call(null,_,opts);
}
});

thi.ng.geom.svg.core.color_attrib = (function thi$ng$geom$svg$core$color_attrib(attribs,id,id2,f){
var temp__4423__auto__ = attribs.call(null,id);
if(cljs.core.truth_(temp__4423__auto__)){
var att = temp__4423__auto__;
if(typeof att === 'string'){
return attribs;
} else {
return cljs.core.assoc.call(null,cljs.core.dissoc.call(null,attribs,id),id2,cljs.core.apply.call(null,f,att));
}
} else {
return attribs;
}
});
thi.ng.geom.svg.core.matrix_attrib = (function thi$ng$geom$svg$core$matrix_attrib(attribs,id){
var temp__4423__auto__ = attribs.call(null,id);
if(cljs.core.truth_(temp__4423__auto__)){
var mat = temp__4423__auto__;
if(typeof mat === 'string'){
return attribs;
} else {
var vec__30823 = mat;
var a = cljs.core.nth.call(null,vec__30823,(0),null);
var c = cljs.core.nth.call(null,vec__30823,(1),null);
var e = cljs.core.nth.call(null,vec__30823,(2),null);
var b = cljs.core.nth.call(null,vec__30823,(3),null);
var d = cljs.core.nth.call(null,vec__30823,(4),null);
var f = cljs.core.nth.call(null,vec__30823,(5),null);
return cljs.core.assoc.call(null,attribs,id,cljs.core.apply.call(null,thi.ng.strf.core.format,thi.ng.geom.svg.core._STAR_fmt_matrix_STAR_,new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [a,b,c,d,e,f], null)));
}
} else {
return attribs;
}
});
thi.ng.geom.svg.core.filter_attribs = (function thi$ng$geom$svg$core$filter_attribs(attribs){
var acc = cljs.core.transient$.call(null,attribs);
var ks = cljs.core.keys.call(null,attribs);
while(true){
if(cljs.core.truth_(ks)){
var G__30824 = ((cljs.core._EQ_.call(null,"__",cljs.core.subs.call(null,cljs.core.name.call(null,cljs.core.first.call(null,ks)),(0),(2))))?cljs.core.dissoc_BANG_.call(null,acc,cljs.core.first.call(null,ks)):acc);
var G__30825 = cljs.core.next.call(null,ks);
acc = G__30824;
ks = G__30825;
continue;
} else {
return cljs.core.persistent_BANG_.call(null,acc);
}
break;
}
});
thi.ng.geom.svg.core.svg_attribs = (function thi$ng$geom$svg$core$svg_attribs(attribs,base){
if(cljs.core.seq.call(null,attribs)){
return cljs.core.into.call(null,thi.ng.geom.svg.core.matrix_attrib.call(null,thi.ng.geom.svg.core.color_attrib.call(null,thi.ng.geom.svg.core.color_attrib.call(null,thi.ng.geom.svg.core.color_attrib.call(null,thi.ng.geom.svg.core.color_attrib.call(null,thi.ng.geom.svg.core.filter_attribs.call(null,attribs),new cljs.core.Keyword(null,"stroke","stroke",1741823555),new cljs.core.Keyword(null,"stroke","stroke",1741823555),thi.ng.color.core.rgba__GT_css),new cljs.core.Keyword(null,"stroke-hsv","stroke-hsv",-877154955),new cljs.core.Keyword(null,"stroke","stroke",1741823555),thi.ng.color.core.hsva__GT_css),new cljs.core.Keyword(null,"fill","fill",883462889),new cljs.core.Keyword(null,"fill","fill",883462889),thi.ng.color.core.rgba__GT_css),new cljs.core.Keyword(null,"fill-hsv","fill-hsv",-968301006),new cljs.core.Keyword(null,"fill","fill",883462889),thi.ng.color.core.hsva__GT_css),new cljs.core.Keyword(null,"transform","transform",1381301764)),base);
} else {
return base;
}
});
thi.ng.geom.svg.core.svg = (function thi$ng$geom$svg$core$svg(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.geom.svg.core.svg.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.svg.cljs$core$IFn$_invoke$arity$variadic = (function (attribs,body){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"svg","svg",856789142),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 3, ["xmlns","http://www.w3.org/2000/svg","xmlns:xlink","http://www.w3.org/1999/xlink","version","1.1"], null)),body], null);
});

thi.ng.geom.svg.core.svg.cljs$lang$maxFixedArity = (1);

thi.ng.geom.svg.core.svg.cljs$lang$applyTo = (function (seq30826){
var G__30827 = cljs.core.first.call(null,seq30826);
var seq30826__$1 = cljs.core.next.call(null,seq30826);
return thi.ng.geom.svg.core.svg.cljs$core$IFn$_invoke$arity$variadic(G__30827,seq30826__$1);
});
thi.ng.geom.svg.core.defs = (function thi$ng$geom$svg$core$defs(){
var argseq__17109__auto__ = ((((0) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(0)),(0))):null);
return thi.ng.geom.svg.core.defs.cljs$core$IFn$_invoke$arity$variadic(argseq__17109__auto__);
});

thi.ng.geom.svg.core.defs.cljs$core$IFn$_invoke$arity$variadic = (function (defs){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"defs","defs",1398449717),defs], null);
});

thi.ng.geom.svg.core.defs.cljs$lang$maxFixedArity = (0);

thi.ng.geom.svg.core.defs.cljs$lang$applyTo = (function (seq30828){
return thi.ng.geom.svg.core.defs.cljs$core$IFn$_invoke$arity$variadic(cljs.core.seq.call(null,seq30828));
});
thi.ng.geom.svg.core.gradient_stop = (function thi$ng$geom$svg$core$gradient_stop(f,p__30829){
var vec__30831 = p__30829;
var pos = cljs.core.nth.call(null,vec__30831,(0),null);
var col = cljs.core.nth.call(null,vec__30831,(1),null);
var col__$1 = ((typeof col === 'string')?col:cljs.core.apply.call(null,f,col));
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"stop","stop",-2140911342),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"offset","offset",296498311),thi.ng.geom.svg.core._STAR_fmt_percent_STAR_.call(null,pos),new cljs.core.Keyword(null,"stop-color","stop-color",316173955),col__$1], null)], null);
});
thi.ng.geom.svg.core.linear_gradient_rgb = (function thi$ng$geom$svg$core$linear_gradient_rgb(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.geom.svg.core.linear_gradient_rgb.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.linear_gradient_rgb.cljs$core$IFn$_invoke$arity$variadic = (function (id,attribs,stops){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"linearGradient","linearGradient",1711964727),cljs.core.assoc.call(null,attribs,new cljs.core.Keyword(null,"id","id",-1388402092),id),cljs.core.map.call(null,(function (p1__30832_SHARP_){
return thi.ng.geom.svg.core.gradient_stop.call(null,thi.ng.color.core.rgba__GT_css,p1__30832_SHARP_);
}),stops)], null);
});

thi.ng.geom.svg.core.linear_gradient_rgb.cljs$lang$maxFixedArity = (2);

thi.ng.geom.svg.core.linear_gradient_rgb.cljs$lang$applyTo = (function (seq30833){
var G__30834 = cljs.core.first.call(null,seq30833);
var seq30833__$1 = cljs.core.next.call(null,seq30833);
var G__30835 = cljs.core.first.call(null,seq30833__$1);
var seq30833__$2 = cljs.core.next.call(null,seq30833__$1);
return thi.ng.geom.svg.core.linear_gradient_rgb.cljs$core$IFn$_invoke$arity$variadic(G__30834,G__30835,seq30833__$2);
});
thi.ng.geom.svg.core.radial_gradient_rgb = (function thi$ng$geom$svg$core$radial_gradient_rgb(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.geom.svg.core.radial_gradient_rgb.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.radial_gradient_rgb.cljs$core$IFn$_invoke$arity$variadic = (function (id,attribs,stops){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"radialGradient","radialGradient",1402247193),cljs.core.assoc.call(null,attribs,new cljs.core.Keyword(null,"id","id",-1388402092),id),cljs.core.map.call(null,(function (p1__30836_SHARP_){
return thi.ng.geom.svg.core.gradient_stop.call(null,thi.ng.color.core.rgba__GT_css,p1__30836_SHARP_);
}),stops)], null);
});

thi.ng.geom.svg.core.radial_gradient_rgb.cljs$lang$maxFixedArity = (2);

thi.ng.geom.svg.core.radial_gradient_rgb.cljs$lang$applyTo = (function (seq30837){
var G__30838 = cljs.core.first.call(null,seq30837);
var seq30837__$1 = cljs.core.next.call(null,seq30837);
var G__30839 = cljs.core.first.call(null,seq30837__$1);
var seq30837__$2 = cljs.core.next.call(null,seq30837__$1);
return thi.ng.geom.svg.core.radial_gradient_rgb.cljs$core$IFn$_invoke$arity$variadic(G__30838,G__30839,seq30837__$2);
});
thi.ng.geom.svg.core.linear_gradient_hsv = (function thi$ng$geom$svg$core$linear_gradient_hsv(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.geom.svg.core.linear_gradient_hsv.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.linear_gradient_hsv.cljs$core$IFn$_invoke$arity$variadic = (function (id,attribs,stops){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"linearGradient","linearGradient",1711964727),cljs.core.assoc.call(null,attribs,new cljs.core.Keyword(null,"id","id",-1388402092),id),cljs.core.map.call(null,(function (p1__30840_SHARP_){
return thi.ng.geom.svg.core.gradient_stop.call(null,thi.ng.color.core.hsva__GT_css,p1__30840_SHARP_);
}),stops)], null);
});

thi.ng.geom.svg.core.linear_gradient_hsv.cljs$lang$maxFixedArity = (2);

thi.ng.geom.svg.core.linear_gradient_hsv.cljs$lang$applyTo = (function (seq30841){
var G__30842 = cljs.core.first.call(null,seq30841);
var seq30841__$1 = cljs.core.next.call(null,seq30841);
var G__30843 = cljs.core.first.call(null,seq30841__$1);
var seq30841__$2 = cljs.core.next.call(null,seq30841__$1);
return thi.ng.geom.svg.core.linear_gradient_hsv.cljs$core$IFn$_invoke$arity$variadic(G__30842,G__30843,seq30841__$2);
});
thi.ng.geom.svg.core.radial_gradient_hsv = (function thi$ng$geom$svg$core$radial_gradient_hsv(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.geom.svg.core.radial_gradient_hsv.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.radial_gradient_hsv.cljs$core$IFn$_invoke$arity$variadic = (function (id,attribs,stops){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"radialGradient","radialGradient",1402247193),cljs.core.assoc.call(null,attribs,new cljs.core.Keyword(null,"id","id",-1388402092),id),cljs.core.map.call(null,(function (p1__30844_SHARP_){
return thi.ng.geom.svg.core.gradient_stop.call(null,thi.ng.color.core.hsva__GT_css,p1__30844_SHARP_);
}),stops)], null);
});

thi.ng.geom.svg.core.radial_gradient_hsv.cljs$lang$maxFixedArity = (2);

thi.ng.geom.svg.core.radial_gradient_hsv.cljs$lang$applyTo = (function (seq30845){
var G__30846 = cljs.core.first.call(null,seq30845);
var seq30845__$1 = cljs.core.next.call(null,seq30845);
var G__30847 = cljs.core.first.call(null,seq30845__$1);
var seq30845__$2 = cljs.core.next.call(null,seq30845__$1);
return thi.ng.geom.svg.core.radial_gradient_hsv.cljs$core$IFn$_invoke$arity$variadic(G__30846,G__30847,seq30845__$2);
});
thi.ng.geom.svg.core.group = (function thi$ng$geom$svg$core$group(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.geom.svg.core.group.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.group.cljs$core$IFn$_invoke$arity$variadic = (function (attribs,body){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"g","g",1738089905),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,null),body], null);
});

thi.ng.geom.svg.core.group.cljs$lang$maxFixedArity = (1);

thi.ng.geom.svg.core.group.cljs$lang$applyTo = (function (seq30848){
var G__30849 = cljs.core.first.call(null,seq30848);
var seq30848__$1 = cljs.core.next.call(null,seq30848);
return thi.ng.geom.svg.core.group.cljs$core$IFn$_invoke$arity$variadic(G__30849,seq30848__$1);
});
thi.ng.geom.svg.core.path = (function thi$ng$geom$svg$core$path(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.geom.svg.core.path.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.path.cljs$core$IFn$_invoke$arity$variadic = (function (segments,p__30852){
var vec__30853 = p__30852;
var attribs = cljs.core.nth.call(null,vec__30853,(0),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"path","path",-188191168),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"d","d",1972142424),cljs.core.apply.call(null,thi.ng.strf.core.format,cljs.core.mapcat.call(null,cljs.core.comp.call(null,thi.ng.geom.svg.core.path_segment_formats,cljs.core.first),segments),cljs.core.mapcat.call(null,cljs.core.rest,segments))], null))], null);
});

thi.ng.geom.svg.core.path.cljs$lang$maxFixedArity = (1);

thi.ng.geom.svg.core.path.cljs$lang$applyTo = (function (seq30850){
var G__30851 = cljs.core.first.call(null,seq30850);
var seq30850__$1 = cljs.core.next.call(null,seq30850);
return thi.ng.geom.svg.core.path.cljs$core$IFn$_invoke$arity$variadic(G__30851,seq30850__$1);
});
thi.ng.geom.svg.core.text = (function thi$ng$geom$svg$core$text(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.geom.svg.core.text.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.text.cljs$core$IFn$_invoke$arity$variadic = (function (p__30857,txt,p__30858){
var vec__30859 = p__30857;
var x = cljs.core.nth.call(null,vec__30859,(0),null);
var y = cljs.core.nth.call(null,vec__30859,(1),null);
var vec__30860 = p__30858;
var attribs = cljs.core.nth.call(null,vec__30860,(0),null);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"text","text",-1790561697),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"x","x",2099068185),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,x),new cljs.core.Keyword(null,"y","y",-1757859776),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,y)], null)),txt], null);
});

thi.ng.geom.svg.core.text.cljs$lang$maxFixedArity = (2);

thi.ng.geom.svg.core.text.cljs$lang$applyTo = (function (seq30854){
var G__30855 = cljs.core.first.call(null,seq30854);
var seq30854__$1 = cljs.core.next.call(null,seq30854);
var G__30856 = cljs.core.first.call(null,seq30854__$1);
var seq30854__$2 = cljs.core.next.call(null,seq30854__$1);
return thi.ng.geom.svg.core.text.cljs$core$IFn$_invoke$arity$variadic(G__30855,G__30856,seq30854__$2);
});
thi.ng.geom.svg.core.circle = (function thi$ng$geom$svg$core$circle(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.geom.svg.core.circle.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.circle.cljs$core$IFn$_invoke$arity$variadic = (function (p__30864,radius,p__30865){
var vec__30866 = p__30864;
var x = cljs.core.nth.call(null,vec__30866,(0),null);
var y = cljs.core.nth.call(null,vec__30866,(1),null);
var vec__30867 = p__30865;
var attribs = cljs.core.nth.call(null,vec__30867,(0),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"circle","circle",1903212362),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"cx","cx",1272694324),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,x),new cljs.core.Keyword(null,"cy","cy",755331060),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,y),new cljs.core.Keyword(null,"r","r",-471384190),radius], null))], null);
});

thi.ng.geom.svg.core.circle.cljs$lang$maxFixedArity = (2);

thi.ng.geom.svg.core.circle.cljs$lang$applyTo = (function (seq30861){
var G__30862 = cljs.core.first.call(null,seq30861);
var seq30861__$1 = cljs.core.next.call(null,seq30861);
var G__30863 = cljs.core.first.call(null,seq30861__$1);
var seq30861__$2 = cljs.core.next.call(null,seq30861__$1);
return thi.ng.geom.svg.core.circle.cljs$core$IFn$_invoke$arity$variadic(G__30862,G__30863,seq30861__$2);
});
thi.ng.geom.svg.core.arc = (function thi$ng$geom$svg$core$arc(){
var argseq__17109__auto__ = ((((6) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(6)),(0))):null);
return thi.ng.geom.svg.core.arc.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]),(arguments[(5)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.arc.cljs$core$IFn$_invoke$arity$variadic = (function (center,radius,theta1,theta2,great_QMARK_,ccw_QMARK_,p__30875){
var vec__30876 = p__30875;
var attribs = cljs.core.nth.call(null,vec__30876,(0),null);
var radius__$1 = thi.ng.geom.core.vector.vec2.call(null,radius);
var p = thi.ng.geom.core._PLUS_.call(null,thi.ng.geom.core.vector.vec2.call(null,center),thi.ng.geom.core.as_cartesian.call(null,thi.ng.geom.core.vector.vec2.call(null,thi.ng.geom.core.vector.x.call(null,radius__$1),theta1)));
var q = thi.ng.geom.core._PLUS_.call(null,thi.ng.geom.core.vector.vec2.call(null,center),thi.ng.geom.core.as_cartesian.call(null,thi.ng.geom.core.vector.vec2.call(null,thi.ng.geom.core.vector.y.call(null,radius__$1),theta2)));
return thi.ng.geom.svg.core.path.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"M","M",-1755742206),p], null),new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"A","A",-1688942394),radius__$1,(0),(cljs.core.truth_(great_QMARK_)?(1):(0)),(cljs.core.truth_(ccw_QMARK_)?(1):(0)),q], null)], null),attribs);
});

thi.ng.geom.svg.core.arc.cljs$lang$maxFixedArity = (6);

thi.ng.geom.svg.core.arc.cljs$lang$applyTo = (function (seq30868){
var G__30869 = cljs.core.first.call(null,seq30868);
var seq30868__$1 = cljs.core.next.call(null,seq30868);
var G__30870 = cljs.core.first.call(null,seq30868__$1);
var seq30868__$2 = cljs.core.next.call(null,seq30868__$1);
var G__30871 = cljs.core.first.call(null,seq30868__$2);
var seq30868__$3 = cljs.core.next.call(null,seq30868__$2);
var G__30872 = cljs.core.first.call(null,seq30868__$3);
var seq30868__$4 = cljs.core.next.call(null,seq30868__$3);
var G__30873 = cljs.core.first.call(null,seq30868__$4);
var seq30868__$5 = cljs.core.next.call(null,seq30868__$4);
var G__30874 = cljs.core.first.call(null,seq30868__$5);
var seq30868__$6 = cljs.core.next.call(null,seq30868__$5);
return thi.ng.geom.svg.core.arc.cljs$core$IFn$_invoke$arity$variadic(G__30869,G__30870,G__30871,G__30872,G__30873,G__30874,seq30868__$6);
});
thi.ng.geom.svg.core.rect = (function thi$ng$geom$svg$core$rect(){
var argseq__17109__auto__ = ((((3) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(3)),(0))):null);
return thi.ng.geom.svg.core.rect.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.rect.cljs$core$IFn$_invoke$arity$variadic = (function (p__30881,w,h,p__30882){
var vec__30883 = p__30881;
var x = cljs.core.nth.call(null,vec__30883,(0),null);
var y = cljs.core.nth.call(null,vec__30883,(1),null);
var vec__30884 = p__30882;
var attribs = cljs.core.nth.call(null,vec__30884,(0),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"rect","rect",-108902628),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"x","x",2099068185),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,x),new cljs.core.Keyword(null,"y","y",-1757859776),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,y),new cljs.core.Keyword(null,"width","width",-384071477),w,new cljs.core.Keyword(null,"height","height",1025178622),h], null))], null);
});

thi.ng.geom.svg.core.rect.cljs$lang$maxFixedArity = (3);

thi.ng.geom.svg.core.rect.cljs$lang$applyTo = (function (seq30877){
var G__30878 = cljs.core.first.call(null,seq30877);
var seq30877__$1 = cljs.core.next.call(null,seq30877);
var G__30879 = cljs.core.first.call(null,seq30877__$1);
var seq30877__$2 = cljs.core.next.call(null,seq30877__$1);
var G__30880 = cljs.core.first.call(null,seq30877__$2);
var seq30877__$3 = cljs.core.next.call(null,seq30877__$2);
return thi.ng.geom.svg.core.rect.cljs$core$IFn$_invoke$arity$variadic(G__30878,G__30879,G__30880,seq30877__$3);
});
thi.ng.geom.svg.core.line = (function thi$ng$geom$svg$core$line(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.geom.svg.core.line.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.line.cljs$core$IFn$_invoke$arity$variadic = (function (p__30888,p__30889,p__30890){
var vec__30891 = p__30888;
var ax = cljs.core.nth.call(null,vec__30891,(0),null);
var ay = cljs.core.nth.call(null,vec__30891,(1),null);
var vec__30892 = p__30889;
var bx = cljs.core.nth.call(null,vec__30892,(0),null);
var by = cljs.core.nth.call(null,vec__30892,(1),null);
var vec__30893 = p__30890;
var attribs = cljs.core.nth.call(null,vec__30893,(0),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"line","line",212345235),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"x1","x1",-1863922247),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,ax),new cljs.core.Keyword(null,"y1","y1",589123466),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,ay),new cljs.core.Keyword(null,"x2","x2",-1362513475),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,bx),new cljs.core.Keyword(null,"y2","y2",-718691301),thi.ng.geom.svg.core._STAR_ff_STAR_.call(null,by)], null))], null);
});

thi.ng.geom.svg.core.line.cljs$lang$maxFixedArity = (2);

thi.ng.geom.svg.core.line.cljs$lang$applyTo = (function (seq30885){
var G__30886 = cljs.core.first.call(null,seq30885);
var seq30885__$1 = cljs.core.next.call(null,seq30885);
var G__30887 = cljs.core.first.call(null,seq30885__$1);
var seq30885__$2 = cljs.core.next.call(null,seq30885__$1);
return thi.ng.geom.svg.core.line.cljs$core$IFn$_invoke$arity$variadic(G__30886,G__30887,seq30885__$2);
});
thi.ng.geom.svg.core.line_decorated = (function thi$ng$geom$svg$core$line_decorated(){
var argseq__17109__auto__ = ((((4) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(4)),(0))):null);
return thi.ng.geom.svg.core.line_decorated.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.line_decorated.cljs$core$IFn$_invoke$arity$variadic = (function (p,q,start,end,p__30899){
var vec__30900 = p__30899;
var attribs = cljs.core.nth.call(null,vec__30900,(0),null);
return cljs.core._conj.call(null,cljs.core._conj.call(null,cljs.core._conj.call(null,cljs.core.List.EMPTY,(cljs.core.truth_(end)?end.call(null,p,q,(0),attribs):null)),(cljs.core.truth_(start)?start.call(null,q,p,(0),attribs):null)),thi.ng.geom.svg.core.line.call(null,p,q,attribs));
});

thi.ng.geom.svg.core.line_decorated.cljs$lang$maxFixedArity = (4);

thi.ng.geom.svg.core.line_decorated.cljs$lang$applyTo = (function (seq30894){
var G__30895 = cljs.core.first.call(null,seq30894);
var seq30894__$1 = cljs.core.next.call(null,seq30894);
var G__30896 = cljs.core.first.call(null,seq30894__$1);
var seq30894__$2 = cljs.core.next.call(null,seq30894__$1);
var G__30897 = cljs.core.first.call(null,seq30894__$2);
var seq30894__$3 = cljs.core.next.call(null,seq30894__$2);
var G__30898 = cljs.core.first.call(null,seq30894__$3);
var seq30894__$4 = cljs.core.next.call(null,seq30894__$3);
return thi.ng.geom.svg.core.line_decorated.cljs$core$IFn$_invoke$arity$variadic(G__30895,G__30896,G__30897,G__30898,seq30894__$4);
});
thi.ng.geom.svg.core.line_strip = (function thi$ng$geom$svg$core$line_strip(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.geom.svg.core.line_strip.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.line_strip.cljs$core$IFn$_invoke$arity$variadic = (function (points,p__30903){
var vec__30904 = p__30903;
var attribs = cljs.core.nth.call(null,vec__30904,(0),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"polyline","polyline",-1731551044),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"fill","fill",883462889),"none",new cljs.core.Keyword(null,"points","points",-1486596883),cljs.core.apply.call(null,thi.ng.strf.core.format,thi.ng.geom.svg.core.point_seq_format.call(null,cljs.core.count.call(null,points)),points)], null))], null);
});

thi.ng.geom.svg.core.line_strip.cljs$lang$maxFixedArity = (1);

thi.ng.geom.svg.core.line_strip.cljs$lang$applyTo = (function (seq30901){
var G__30902 = cljs.core.first.call(null,seq30901);
var seq30901__$1 = cljs.core.next.call(null,seq30901);
return thi.ng.geom.svg.core.line_strip.cljs$core$IFn$_invoke$arity$variadic(G__30902,seq30901__$1);
});
thi.ng.geom.svg.core.line_strip_decorated = (function thi$ng$geom$svg$core$line_strip_decorated(){
var argseq__17109__auto__ = ((((4) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(4)),(0))):null);
return thi.ng.geom.svg.core.line_strip_decorated.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.line_strip_decorated.cljs$core$IFn$_invoke$arity$variadic = (function (points,start,seg,end,p__30910){
var vec__30911 = p__30910;
var attribs = cljs.core.nth.call(null,vec__30911,(0),null);
var n = (cljs.core.count.call(null,points) - (1));
return cljs.core._conj.call(null,cljs.core._conj.call(null,cljs.core._conj.call(null,cljs.core._conj.call(null,cljs.core.List.EMPTY,(cljs.core.truth_(end)?end.call(null,points.call(null,(n - (1))),cljs.core.peek.call(null,points),n,attribs):null)),(cljs.core.truth_(seg)?cljs.core.map_indexed.call(null,((function (n,vec__30911,attribs){
return (function (i,p__30912){
var vec__30913 = p__30912;
var p = cljs.core.nth.call(null,vec__30913,(0),null);
var q = cljs.core.nth.call(null,vec__30913,(1),null);
return seg.call(null,p,q,i,attribs);
});})(n,vec__30911,attribs))
,thi.ng.dstruct.core.successive_nth.call(null,(2),points)):null)),(cljs.core.truth_(start)?start.call(null,points.call(null,(1)),points.call(null,(0)),(0),attribs):null)),thi.ng.geom.svg.core.line_strip.call(null,points,attribs));
});

thi.ng.geom.svg.core.line_strip_decorated.cljs$lang$maxFixedArity = (4);

thi.ng.geom.svg.core.line_strip_decorated.cljs$lang$applyTo = (function (seq30905){
var G__30906 = cljs.core.first.call(null,seq30905);
var seq30905__$1 = cljs.core.next.call(null,seq30905);
var G__30907 = cljs.core.first.call(null,seq30905__$1);
var seq30905__$2 = cljs.core.next.call(null,seq30905__$1);
var G__30908 = cljs.core.first.call(null,seq30905__$2);
var seq30905__$3 = cljs.core.next.call(null,seq30905__$2);
var G__30909 = cljs.core.first.call(null,seq30905__$3);
var seq30905__$4 = cljs.core.next.call(null,seq30905__$3);
return thi.ng.geom.svg.core.line_strip_decorated.cljs$core$IFn$_invoke$arity$variadic(G__30906,G__30907,G__30908,G__30909,seq30905__$4);
});
thi.ng.geom.svg.core.polygon = (function thi$ng$geom$svg$core$polygon(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.geom.svg.core.polygon.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.polygon.cljs$core$IFn$_invoke$arity$variadic = (function (points,p__30916){
var vec__30917 = p__30916;
var attribs = cljs.core.nth.call(null,vec__30917,(0),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"polygon","polygon",837053759),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"points","points",-1486596883),cljs.core.apply.call(null,thi.ng.strf.core.format,thi.ng.geom.svg.core.point_seq_format.call(null,cljs.core.count.call(null,points)),points)], null))], null);
});

thi.ng.geom.svg.core.polygon.cljs$lang$maxFixedArity = (1);

thi.ng.geom.svg.core.polygon.cljs$lang$applyTo = (function (seq30914){
var G__30915 = cljs.core.first.call(null,seq30914);
var seq30914__$1 = cljs.core.next.call(null,seq30914);
return thi.ng.geom.svg.core.polygon.cljs$core$IFn$_invoke$arity$variadic(G__30915,seq30914__$1);
});
thi.ng.geom.svg.core.instance = (function thi$ng$geom$svg$core$instance(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.geom.svg.core.instance.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.instance.cljs$core$IFn$_invoke$arity$variadic = (function (id,p__30920){
var vec__30921 = p__30920;
var attribs = cljs.core.nth.call(null,vec__30921,(0),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"use","use",-1846382424),thi.ng.geom.svg.core.svg_attribs.call(null,attribs,new cljs.core.PersistentArrayMap(null, 1, ["xlink:href",[cljs.core.str("#"),cljs.core.str(id)].join('')], null))], null);
});

thi.ng.geom.svg.core.instance.cljs$lang$maxFixedArity = (1);

thi.ng.geom.svg.core.instance.cljs$lang$applyTo = (function (seq30918){
var G__30919 = cljs.core.first.call(null,seq30918);
var seq30918__$1 = cljs.core.next.call(null,seq30918);
return thi.ng.geom.svg.core.instance.cljs$core$IFn$_invoke$arity$variadic(G__30919,seq30918__$1);
});
thi.ng.geom.svg.core.arrow_head = (function thi$ng$geom$svg$core$arrow_head(){
var argseq__17109__auto__ = ((((3) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(3)),(0))):null);
return thi.ng.geom.svg.core.arrow_head.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),argseq__17109__auto__);
});

thi.ng.geom.svg.core.arrow_head.cljs$core$IFn$_invoke$arity$variadic = (function (len,theta,solid_QMARK_,p__30926){
var vec__30927 = p__30926;
var opts = cljs.core.nth.call(null,vec__30927,(0),null);
return ((function (vec__30927,opts){
return (function() { 
var G__30930__delegate = function (p,q,idx,p__30928){
var vec__30929 = p__30928;
var attribs = cljs.core.nth.call(null,vec__30929,(0),null);
var q__$1 = thi.ng.geom.core.vector.vec2.call(null,q);
var d = thi.ng.geom.core.normalize.call(null,thi.ng.geom.core._.call(null,q__$1,p),len);
return cljs.core._conj.call(null,cljs.core.List.EMPTY,(cljs.core.truth_(solid_QMARK_)?thi.ng.geom.svg.core.polygon:thi.ng.geom.svg.core.line_strip).call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.core._.call(null,q__$1,thi.ng.geom.core.rotate.call(null,d,(- theta))),q__$1,thi.ng.geom.core._.call(null,q__$1,thi.ng.geom.core.rotate.call(null,d,theta))], null),cljs.core.merge.call(null,attribs,opts)));
};
var G__30930 = function (p,q,idx,var_args){
var p__30928 = null;
if (arguments.length > 3) {
var G__30931__i = 0, G__30931__a = new Array(arguments.length -  3);
while (G__30931__i < G__30931__a.length) {G__30931__a[G__30931__i] = arguments[G__30931__i + 3]; ++G__30931__i;}
  p__30928 = new cljs.core.IndexedSeq(G__30931__a,0);
} 
return G__30930__delegate.call(this,p,q,idx,p__30928);};
G__30930.cljs$lang$maxFixedArity = 3;
G__30930.cljs$lang$applyTo = (function (arglist__30932){
var p = cljs.core.first(arglist__30932);
arglist__30932 = cljs.core.next(arglist__30932);
var q = cljs.core.first(arglist__30932);
arglist__30932 = cljs.core.next(arglist__30932);
var idx = cljs.core.first(arglist__30932);
var p__30928 = cljs.core.rest(arglist__30932);
return G__30930__delegate(p,q,idx,p__30928);
});
G__30930.cljs$core$IFn$_invoke$arity$variadic = G__30930__delegate;
return G__30930;
})()
;
;})(vec__30927,opts))
});

thi.ng.geom.svg.core.arrow_head.cljs$lang$maxFixedArity = (3);

thi.ng.geom.svg.core.arrow_head.cljs$lang$applyTo = (function (seq30922){
var G__30923 = cljs.core.first.call(null,seq30922);
var seq30922__$1 = cljs.core.next.call(null,seq30922);
var G__30924 = cljs.core.first.call(null,seq30922__$1);
var seq30922__$2 = cljs.core.next.call(null,seq30922__$1);
var G__30925 = cljs.core.first.call(null,seq30922__$2);
var seq30922__$3 = cljs.core.next.call(null,seq30922__$2);
return thi.ng.geom.svg.core.arrow_head.cljs$core$IFn$_invoke$arity$variadic(G__30923,G__30924,G__30925,seq30922__$3);
});
thi.ng.geom.svg.core.line_label = (function thi$ng$geom$svg$core$line_label(){
var argseq__17109__auto__ = ((((0) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(0)),(0))):null);
return thi.ng.geom.svg.core.line_label.cljs$core$IFn$_invoke$arity$variadic(argseq__17109__auto__);
});

thi.ng.geom.svg.core.line_label.cljs$core$IFn$_invoke$arity$variadic = (function (p__30935){
var vec__30936 = p__30935;
var map__30937 = cljs.core.nth.call(null,vec__30936,(0),null);
var map__30937__$1 = ((cljs.core.seq_QMARK_.call(null,map__30937))?cljs.core.apply.call(null,cljs.core.hash_map,map__30937):map__30937);
var opts = map__30937__$1;
var __rotate_QMARK_ = cljs.core.get.call(null,map__30937__$1,new cljs.core.Keyword(null,"__rotate?","__rotate?",-212603580));
var __offset = cljs.core.get.call(null,map__30937__$1,new cljs.core.Keyword(null,"__offset","__offset",-9560190));
var opts__$1 = cljs.core.update_in.call(null,cljs.core.dissoc.call(null,opts,new cljs.core.Keyword(null,"__rotate?","__rotate?",-212603580),new cljs.core.Keyword(null,"__offset","__offset",-9560190)),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"text-anchor","text-anchor",585613696)], null),((function (vec__30936,map__30937,map__30937__$1,opts,__rotate_QMARK_,__offset){
return (function (p1__30933_SHARP_){
var or__16069__auto__ = p1__30933_SHARP_;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return "middle";
}
});})(vec__30936,map__30937,map__30937__$1,opts,__rotate_QMARK_,__offset))
);
return ((function (opts__$1,vec__30936,map__30937,map__30937__$1,opts,__rotate_QMARK_,__offset){
return (function() { 
var G__30940__delegate = function (p,q,idx,p__30938){
var vec__30939 = p__30938;
var attribs = cljs.core.nth.call(null,vec__30939,(0),null);
var temp__4423__auto__ = cljs.core.get_in.call(null,attribs,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"__label","__label",758805562),idx], null));
if(cljs.core.truth_(temp__4423__auto__)){
var label = temp__4423__auto__;
var p__$1 = thi.ng.geom.core.vector.vec2.call(null,p);
var m = thi.ng.geom.core._PLUS_.call(null,thi.ng.geom.core.mix.call(null,p__$1,q),__offset);
var opts__$2 = (cljs.core.truth_(__rotate_QMARK_)?cljs.core.assoc.call(null,opts__$1,new cljs.core.Keyword(null,"transform","transform",1381301764),[cljs.core.str("rotate("),cljs.core.str(thi.ng.math.core.degrees.call(null,thi.ng.geom.core.heading.call(null,thi.ng.geom.core.normal.call(null,thi.ng.geom.core._.call(null,p__$1,q))))),cljs.core.str(" "),cljs.core.str(m.call(null,(0))),cljs.core.str(" "),cljs.core.str(m.call(null,(1))),cljs.core.str(")")].join('')):opts__$1);
return cljs.core._conj.call(null,cljs.core.List.EMPTY,thi.ng.geom.svg.core.text.call(null,m,label,cljs.core.merge.call(null,cljs.core.dissoc.call(null,attribs,new cljs.core.Keyword(null,"__label","__label",758805562)),opts__$2)));
} else {
return null;
}
};
var G__30940 = function (p,q,idx,var_args){
var p__30938 = null;
if (arguments.length > 3) {
var G__30941__i = 0, G__30941__a = new Array(arguments.length -  3);
while (G__30941__i < G__30941__a.length) {G__30941__a[G__30941__i] = arguments[G__30941__i + 3]; ++G__30941__i;}
  p__30938 = new cljs.core.IndexedSeq(G__30941__a,0);
} 
return G__30940__delegate.call(this,p,q,idx,p__30938);};
G__30940.cljs$lang$maxFixedArity = 3;
G__30940.cljs$lang$applyTo = (function (arglist__30942){
var p = cljs.core.first(arglist__30942);
arglist__30942 = cljs.core.next(arglist__30942);
var q = cljs.core.first(arglist__30942);
arglist__30942 = cljs.core.next(arglist__30942);
var idx = cljs.core.first(arglist__30942);
var p__30938 = cljs.core.rest(arglist__30942);
return G__30940__delegate(p,q,idx,p__30938);
});
G__30940.cljs$core$IFn$_invoke$arity$variadic = G__30940__delegate;
return G__30940;
})()
;
;})(opts__$1,vec__30936,map__30937,map__30937__$1,opts,__rotate_QMARK_,__offset))
});

thi.ng.geom.svg.core.line_label.cljs$lang$maxFixedArity = (0);

thi.ng.geom.svg.core.line_label.cljs$lang$applyTo = (function (seq30934){
return thi.ng.geom.svg.core.line_label.cljs$core$IFn$_invoke$arity$variadic(cljs.core.seq.call(null,seq30934));
});
thi.ng.geom.svg.core.comp_decorators = (function thi$ng$geom$svg$core$comp_decorators(){
var argseq__17109__auto__ = ((((0) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(0)),(0))):null);
return thi.ng.geom.svg.core.comp_decorators.cljs$core$IFn$_invoke$arity$variadic(argseq__17109__auto__);
});

thi.ng.geom.svg.core.comp_decorators.cljs$core$IFn$_invoke$arity$variadic = (function (fns){
return (function() { 
var G__30946__delegate = function (p,q,idx,p__30944){
var vec__30945 = p__30944;
var attribs = cljs.core.nth.call(null,vec__30945,(0),null);
return cljs.core.reduce.call(null,((function (vec__30945,attribs){
return (function (acc,f){
return cljs.core.concat.call(null,acc,f.call(null,p,q,idx,attribs));
});})(vec__30945,attribs))
,cljs.core.List.EMPTY,fns);
};
var G__30946 = function (p,q,idx,var_args){
var p__30944 = null;
if (arguments.length > 3) {
var G__30947__i = 0, G__30947__a = new Array(arguments.length -  3);
while (G__30947__i < G__30947__a.length) {G__30947__a[G__30947__i] = arguments[G__30947__i + 3]; ++G__30947__i;}
  p__30944 = new cljs.core.IndexedSeq(G__30947__a,0);
} 
return G__30946__delegate.call(this,p,q,idx,p__30944);};
G__30946.cljs$lang$maxFixedArity = 3;
G__30946.cljs$lang$applyTo = (function (arglist__30948){
var p = cljs.core.first(arglist__30948);
arglist__30948 = cljs.core.next(arglist__30948);
var q = cljs.core.first(arglist__30948);
arglist__30948 = cljs.core.next(arglist__30948);
var idx = cljs.core.first(arglist__30948);
var p__30944 = cljs.core.rest(arglist__30948);
return G__30946__delegate(p,q,idx,p__30944);
});
G__30946.cljs$core$IFn$_invoke$arity$variadic = G__30946__delegate;
return G__30946;
})()
;
});

thi.ng.geom.svg.core.comp_decorators.cljs$lang$maxFixedArity = (0);

thi.ng.geom.svg.core.comp_decorators.cljs$lang$applyTo = (function (seq30943){
return thi.ng.geom.svg.core.comp_decorators.cljs$core$IFn$_invoke$arity$variadic(cljs.core.seq.call(null,seq30943));
});

//# sourceMappingURL=core.js.map?rel=1439206034552
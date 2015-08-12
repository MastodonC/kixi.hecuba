// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.geom.viz.core');
goog.require('cljs.core');
goog.require('thi.ng.geom.core.utils');
goog.require('thi.ng.ndarray.core');
goog.require('thi.ng.geom.core');
goog.require('thi.ng.geom.core.vector');
goog.require('thi.ng.geom.svg.core');
goog.require('thi.ng.math.core');
goog.require('thi.ng.ndarray.contours');
goog.require('thi.ng.strf.core');
thi.ng.geom.viz.core.value_mapper = (function thi$ng$geom$viz$core$value_mapper(scale_x,scale_y){
return (function (p__32588){
var vec__32589 = p__32588;
var x = cljs.core.nth.call(null,vec__32589,(0),null);
var y = cljs.core.nth.call(null,vec__32589,(1),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,x),scale_y.call(null,y)], null);
});
});
thi.ng.geom.viz.core.value_transducer = (function thi$ng$geom$viz$core$value_transducer(p__32592){
var map__32599 = p__32592;
var map__32599__$1 = ((cljs.core.seq_QMARK_.call(null,map__32599))?cljs.core.apply.call(null,cljs.core.hash_map,map__32599):map__32599);
var cull_domain = cljs.core.get.call(null,map__32599__$1,new cljs.core.Keyword(null,"cull-domain","cull-domain",408515057));
var cull_range = cljs.core.get.call(null,map__32599__$1,new cljs.core.Keyword(null,"cull-range","cull-range",603502637));
var scale_x = cljs.core.get.call(null,map__32599__$1,new cljs.core.Keyword(null,"scale-x","scale-x",-13535878));
var scale_y = cljs.core.get.call(null,map__32599__$1,new cljs.core.Keyword(null,"scale-y","scale-y",1326124277));
var project = cljs.core.get.call(null,map__32599__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var shape = cljs.core.get.call(null,map__32599__$1,new cljs.core.Keyword(null,"shape","shape",1190694006));
var item_pos = cljs.core.get.call(null,map__32599__$1,new cljs.core.Keyword(null,"item-pos","item-pos",390857330));
var mapper = thi.ng.geom.viz.core.value_mapper.call(null,scale_x,scale_y);
var item_pos__$1 = (function (){var or__16069__auto__ = item_pos;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return cljs.core.identity;
}
})();
var G__32600 = cljs.core.map.call(null,cljs.core.juxt.call(null,item_pos__$1,cljs.core.identity));
var G__32600__$1 = (cljs.core.truth_(cull_domain)?cljs.core.comp.call(null,G__32600,cljs.core.filter.call(null,((function (G__32600,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos){
return (function (p1__32590_SHARP_){
return thi.ng.math.core.in_range_QMARK_.call(null,cull_domain,cljs.core.ffirst.call(null,p1__32590_SHARP_));
});})(G__32600,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos))
)):G__32600);
var G__32600__$2 = cljs.core.comp.call(null,G__32600__$1,cljs.core.map.call(null,((function (G__32600,G__32600__$1,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos){
return (function (p__32601){
var vec__32602 = p__32601;
var p = cljs.core.nth.call(null,vec__32602,(0),null);
var i = cljs.core.nth.call(null,vec__32602,(1),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [mapper.call(null,p),i], null);
});})(G__32600,G__32600__$1,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos))
))
;
var G__32600__$3 = (cljs.core.truth_(cull_range)?cljs.core.comp.call(null,G__32600__$2,cljs.core.filter.call(null,((function (G__32600,G__32600__$1,G__32600__$2,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos){
return (function (p1__32591_SHARP_){
return thi.ng.math.core.in_range_QMARK_.call(null,cull_range,cljs.core.peek.call(null,cljs.core.first.call(null,p1__32591_SHARP_)));
});})(G__32600,G__32600__$1,G__32600__$2,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos))
)):G__32600__$2);
var G__32600__$4 = (cljs.core.truth_(project)?cljs.core.comp.call(null,G__32600__$3,cljs.core.map.call(null,((function (G__32600,G__32600__$1,G__32600__$2,G__32600__$3,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos){
return (function (p__32603){
var vec__32604 = p__32603;
var p = cljs.core.nth.call(null,vec__32604,(0),null);
var i = cljs.core.nth.call(null,vec__32604,(1),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [project.call(null,p),i], null);
});})(G__32600,G__32600__$1,G__32600__$2,G__32600__$3,mapper,item_pos__$1,map__32599,map__32599__$1,cull_domain,cull_range,scale_x,scale_y,project,shape,item_pos))
)):G__32600__$3);
var G__32600__$5 = (cljs.core.truth_(shape)?cljs.core.comp.call(null,G__32600__$4,cljs.core.map.call(null,shape)):G__32600__$4);
return G__32600__$5;
});
thi.ng.geom.viz.core.process_points = (function thi$ng$geom$viz$core$process_points(p__32605,p__32606){
var map__32610 = p__32605;
var map__32610__$1 = ((cljs.core.seq_QMARK_.call(null,map__32610))?cljs.core.apply.call(null,cljs.core.hash_map,map__32610):map__32610);
var x_axis = cljs.core.get.call(null,map__32610__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32610__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var project = cljs.core.get.call(null,map__32610__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var map__32611 = p__32606;
var map__32611__$1 = ((cljs.core.seq_QMARK_.call(null,map__32611))?cljs.core.apply.call(null,cljs.core.hash_map,map__32611):map__32611);
var values = cljs.core.get.call(null,map__32611__$1,new cljs.core.Keyword(null,"values","values",372645556));
var item_pos = cljs.core.get.call(null,map__32611__$1,new cljs.core.Keyword(null,"item-pos","item-pos",390857330));
var shape = cljs.core.get.call(null,map__32611__$1,new cljs.core.Keyword(null,"shape","shape",1190694006));
var vec__32612 = new cljs.core.Keyword(null,"range","range",1639692286).cljs$core$IFn$_invoke$arity$1(y_axis);
var ry1 = cljs.core.nth.call(null,vec__32612,(0),null);
var ry2 = cljs.core.nth.call(null,vec__32612,(1),null);
return cljs.core.sequence.call(null,thi.ng.geom.viz.core.value_transducer.call(null,new cljs.core.PersistentArrayMap(null, 7, [new cljs.core.Keyword(null,"cull-domain","cull-domain",408515057),new cljs.core.Keyword(null,"domain","domain",1847214937).cljs$core$IFn$_invoke$arity$1(x_axis),new cljs.core.Keyword(null,"cull-range","cull-range",603502637),(((ry1 < ry2))?new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [ry1,ry2], null):new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [ry2,ry1], null)),new cljs.core.Keyword(null,"item-pos","item-pos",390857330),item_pos,new cljs.core.Keyword(null,"scale-x","scale-x",-13535878),new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(x_axis),new cljs.core.Keyword(null,"scale-y","scale-y",1326124277),new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis),new cljs.core.Keyword(null,"project","project",1124394579),project,new cljs.core.Keyword(null,"shape","shape",1190694006),shape], null)),(cljs.core.truth_(item_pos)?cljs.core.sort_by.call(null,cljs.core.comp.call(null,cljs.core.first,item_pos),values):cljs.core.sort_by.call(null,cljs.core.first,values)));
});
thi.ng.geom.viz.core.points__GT_path_segments = (function thi$ng$geom$viz$core$points__GT_path_segments(p__32615){
var vec__32617 = p__32615;
var p = cljs.core.nth.call(null,vec__32617,(0),null);
var more = cljs.core.nthnext.call(null,vec__32617,(1));
return cljs.core.reduce.call(null,((function (vec__32617,p,more){
return (function (p1__32613_SHARP_,p2__32614_SHARP_){
return cljs.core.conj.call(null,p1__32613_SHARP_,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"L","L",-1038307519),p2__32614_SHARP_], null));
});})(vec__32617,p,more))
,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"M","M",-1755742206),p], null)], null),more);
});
thi.ng.geom.viz.core.polar_projection = (function thi$ng$geom$viz$core$polar_projection(origin){
var o = thi.ng.geom.core.vector.vec2.call(null,origin);
return ((function (o){
return (function (p__32620){
var vec__32621 = p__32620;
var x = cljs.core.nth.call(null,vec__32621,(0),null);
var y = cljs.core.nth.call(null,vec__32621,(1),null);
return thi.ng.geom.core._PLUS_.call(null,o,thi.ng.geom.core.as_cartesian.call(null,thi.ng.geom.core.vector.vec2.call(null,y,x)));
});
;})(o))
});
thi.ng.geom.viz.core.value_formatter = (function thi$ng$geom$viz$core$value_formatter(prec){
var fmt = new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.strf.core.float$.call(null,prec)], null);
return ((function (fmt){
return (function (x){
return thi.ng.strf.core.format.call(null,fmt,x);
});
;})(fmt))
});
thi.ng.geom.viz.core.format_percent = (function thi$ng$geom$viz$core$format_percent(x){
return [cljs.core.str(((x * (100)) | (0))),cljs.core.str("%")].join('');
});
/**
 * Given a vector of domain bounds and a collection of data values
 * (without domain position), produces a lazy-seq of 2-element vectors
 * representing the values of the original coll uniformly spread over
 * the full domain range, with each of the form: [domain-pos value].
 */
thi.ng.geom.viz.core.uniform_domain_points = (function thi$ng$geom$viz$core$uniform_domain_points(p__32622,values){
var vec__32624 = p__32622;
var d1 = cljs.core.nth.call(null,vec__32624,(0),null);
var d2 = cljs.core.nth.call(null,vec__32624,(1),null);
return cljs.core.map.call(null,((function (vec__32624,d1,d2){
return (function (t,v){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.mix.call(null,d1,d2,t),v], null);
});})(vec__32624,d1,d2))
,thi.ng.math.core.norm_range.call(null,(cljs.core.count.call(null,values) - (1))),values);
});
thi.ng.geom.viz.core.domain_bounds_x = (function thi$ng$geom$viz$core$domain_bounds_x(p1__32625_SHARP_){
return thi.ng.geom.core.utils.axis_bounds.call(null,(0),p1__32625_SHARP_);
});
thi.ng.geom.viz.core.domain_bounds_y = (function thi$ng$geom$viz$core$domain_bounds_y(p1__32626_SHARP_){
return thi.ng.geom.core.utils.axis_bounds.call(null,(1),p1__32626_SHARP_);
});
thi.ng.geom.viz.core.domain_bounds_z = (function thi$ng$geom$viz$core$domain_bounds_z(p1__32627_SHARP_){
return thi.ng.geom.core.utils.axis_bounds.call(null,(2),p1__32627_SHARP_);
});
thi.ng.geom.viz.core.total_domain_bounds = (function thi$ng$geom$viz$core$total_domain_bounds(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.geom.viz.core.total_domain_bounds.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.geom.viz.core.total_domain_bounds.cljs$core$IFn$_invoke$arity$variadic = (function (f,colls){
return cljs.core.transduce.call(null,cljs.core.map.call(null,f),cljs.core.completing.call(null,(function (p__32630,p__32631){
var vec__32632 = p__32630;
var aa = cljs.core.nth.call(null,vec__32632,(0),null);
var ab = cljs.core.nth.call(null,vec__32632,(1),null);
var vec__32633 = p__32631;
var xa = cljs.core.nth.call(null,vec__32633,(0),null);
var xb = cljs.core.nth.call(null,vec__32633,(1),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(function (){var x__16388__auto__ = aa;
var y__16389__auto__ = xa;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})(),(function (){var x__16381__auto__ = ab;
var y__16382__auto__ = xb;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})()], null);
})),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.INF_PLUS_,thi.ng.math.core.INF_], null),colls);
});

thi.ng.geom.viz.core.total_domain_bounds.cljs$lang$maxFixedArity = (1);

thi.ng.geom.viz.core.total_domain_bounds.cljs$lang$applyTo = (function (seq32628){
var G__32629 = cljs.core.first.call(null,seq32628);
var seq32628__$1 = cljs.core.next.call(null,seq32628);
return thi.ng.geom.viz.core.total_domain_bounds.cljs$core$IFn$_invoke$arity$variadic(G__32629,seq32628__$1);
});
thi.ng.geom.viz.core.value_domain_bounds = (function thi$ng$geom$viz$core$value_domain_bounds(mat){
var vals = cljs.core.seq.call(null,mat);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.reduce.call(null,cljs.core.min,vals),cljs.core.reduce.call(null,cljs.core.max,vals)], null);
});
thi.ng.geom.viz.core.linear_scale = (function thi$ng$geom$viz$core$linear_scale(domain,range){
return (function (x){
return thi.ng.math.core.map_interval.call(null,x,domain,range);
});
});
thi.ng.geom.viz.core.log = (function thi$ng$geom$viz$core$log(base){
var lb = Math.log(base);
return ((function (lb){
return (function (p1__32634_SHARP_){
return ((((p1__32634_SHARP_ > (0)))?Math.log(p1__32634_SHARP_):(((p1__32634_SHARP_ < (0)))?(- Math.log((- p1__32634_SHARP_))):(0)
)) / lb);
});
;})(lb))
});
thi.ng.geom.viz.core.log_scale = (function thi$ng$geom$viz$core$log_scale(base,p__32635,p__32636){
var vec__32639 = p__32635;
var d1 = cljs.core.nth.call(null,vec__32639,(0),null);
var d2 = cljs.core.nth.call(null,vec__32639,(1),null);
var domain = vec__32639;
var vec__32640 = p__32636;
var r1 = cljs.core.nth.call(null,vec__32640,(0),null);
var r2 = cljs.core.nth.call(null,vec__32640,(1),null);
var range = vec__32640;
var log_STAR_ = thi.ng.geom.viz.core.log.call(null,base);
var d1l = log_STAR_.call(null,d1);
var dr = (log_STAR_.call(null,d2) - d1l);
return ((function (log_STAR_,d1l,dr,vec__32639,d1,d2,domain,vec__32640,r1,r2,range){
return (function (x){
return thi.ng.math.core.mix.call(null,r1,r2,((log_STAR_.call(null,x) - d1l) / dr));
});
;})(log_STAR_,d1l,dr,vec__32639,d1,d2,domain,vec__32640,r1,r2,range))
});
thi.ng.geom.viz.core.lens_scale = (function thi$ng$geom$viz$core$lens_scale(focus,strength,p__32641,p__32642){
var vec__32645 = p__32641;
var d1 = cljs.core.nth.call(null,vec__32645,(0),null);
var d2 = cljs.core.nth.call(null,vec__32645,(1),null);
var vec__32646 = p__32642;
var r1 = cljs.core.nth.call(null,vec__32646,(0),null);
var r2 = cljs.core.nth.call(null,vec__32646,(1),null);
var dr = (d2 - d1);
var f = ((focus - d1) / dr);
return ((function (dr,f,vec__32645,d1,d2,vec__32646,r1,r2){
return (function (x){
return thi.ng.math.core.mix_lens.call(null,r1,r2,((x - d1) / dr),f,strength);
});
;})(dr,f,vec__32645,d1,d2,vec__32646,r1,r2))
});
thi.ng.geom.viz.core.axis_common_STAR_ = (function thi$ng$geom$viz$core$axis_common_STAR_(p__32647){
var map__32649 = p__32647;
var map__32649__$1 = ((cljs.core.seq_QMARK_.call(null,map__32649))?cljs.core.apply.call(null,cljs.core.hash_map,map__32649):map__32649);
var spec = map__32649__$1;
var visible = cljs.core.get.call(null,map__32649__$1,new cljs.core.Keyword(null,"visible","visible",-1024216805),true);
var major_size = cljs.core.get.call(null,map__32649__$1,new cljs.core.Keyword(null,"major-size","major-size",-698672375),(10));
var minor_size = cljs.core.get.call(null,map__32649__$1,new cljs.core.Keyword(null,"minor-size","minor-size",-1586355109),(5));
var format = cljs.core.get.call(null,map__32649__$1,new cljs.core.Keyword(null,"format","format",-1306924766),thi.ng.geom.viz.core.value_formatter.call(null,(2)));
var attribs = cljs.core.get.call(null,map__32649__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var label = cljs.core.get.call(null,map__32649__$1,new cljs.core.Keyword(null,"label","label",1718410804));
var label_dist = cljs.core.get.call(null,map__32649__$1,new cljs.core.Keyword(null,"label-dist","label-dist",-538260526));
return cljs.core.assoc.call(null,spec,new cljs.core.Keyword(null,"visible","visible",-1024216805),visible,new cljs.core.Keyword(null,"major-size","major-size",-698672375),major_size,new cljs.core.Keyword(null,"minor-size","minor-size",-1586355109),minor_size,new cljs.core.Keyword(null,"format","format",-1306924766),format,new cljs.core.Keyword(null,"attribs","attribs",-137878093),cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"black"], null),attribs),new cljs.core.Keyword(null,"label","label",1718410804),cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"fill","fill",883462889),"black",new cljs.core.Keyword(null,"stroke","stroke",1741823555),"none",new cljs.core.Keyword(null,"font-family","font-family",-667419874),"Arial",new cljs.core.Keyword(null,"font-size","font-size",-1847940346),(10),new cljs.core.Keyword(null,"text-anchor","text-anchor",585613696),"middle"], null),label),new cljs.core.Keyword(null,"label-dist","label-dist",-538260526),(function (){var or__16069__auto__ = label_dist;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return ((10) + major_size);
}
})());
});
thi.ng.geom.viz.core.lin_tick_marks = (function thi$ng$geom$viz$core$lin_tick_marks(p__32651,delta){
var vec__32653 = p__32651;
var d1 = cljs.core.nth.call(null,vec__32653,(0),null);
var d2 = cljs.core.nth.call(null,vec__32653,(1),null);
var dr = (d2 - d1);
var d1_SINGLEQUOTE_ = thi.ng.math.core.roundto.call(null,d1,delta);
return cljs.core.filter.call(null,((function (dr,d1_SINGLEQUOTE_,vec__32653,d1,d2){
return (function (p1__32650_SHARP_){
return thi.ng.math.core.in_range_QMARK_.call(null,d1,d2,p1__32650_SHARP_);
});})(dr,d1_SINGLEQUOTE_,vec__32653,d1,d2))
,cljs.core.range.call(null,d1_SINGLEQUOTE_,(d2 + delta),delta));
});
thi.ng.geom.viz.core.linear_axis = (function thi$ng$geom$viz$core$linear_axis(p__32654){
var map__32656 = p__32654;
var map__32656__$1 = ((cljs.core.seq_QMARK_.call(null,map__32656))?cljs.core.apply.call(null,cljs.core.hash_map,map__32656):map__32656);
var spec = map__32656__$1;
var domain = cljs.core.get.call(null,map__32656__$1,new cljs.core.Keyword(null,"domain","domain",1847214937));
var range = cljs.core.get.call(null,map__32656__$1,new cljs.core.Keyword(null,"range","range",1639692286));
var major = cljs.core.get.call(null,map__32656__$1,new cljs.core.Keyword(null,"major","major",-27376078));
var minor = cljs.core.get.call(null,map__32656__$1,new cljs.core.Keyword(null,"minor","minor",-608536071));
var major_SINGLEQUOTE_ = (cljs.core.truth_(major)?thi.ng.geom.viz.core.lin_tick_marks.call(null,domain,major):null);
var minor_SINGLEQUOTE_ = (cljs.core.truth_(minor)?thi.ng.geom.viz.core.lin_tick_marks.call(null,domain,minor):null);
var minor_SINGLEQUOTE___$1 = (cljs.core.truth_((function (){var and__16057__auto__ = major_SINGLEQUOTE_;
if(cljs.core.truth_(and__16057__auto__)){
return minor_SINGLEQUOTE_;
} else {
return and__16057__auto__;
}
})())?cljs.core.filter.call(null,cljs.core.complement.call(null,cljs.core.set.call(null,major_SINGLEQUOTE_)),minor_SINGLEQUOTE_):minor_SINGLEQUOTE_);
return thi.ng.geom.viz.core.axis_common_STAR_.call(null,cljs.core.assoc.call(null,spec,new cljs.core.Keyword(null,"scale","scale",-230427353),thi.ng.geom.viz.core.linear_scale.call(null,domain,range),new cljs.core.Keyword(null,"major","major",-27376078),major_SINGLEQUOTE_,new cljs.core.Keyword(null,"minor","minor",-608536071),minor_SINGLEQUOTE___$1));
});
thi.ng.geom.viz.core.log_ticks_domain = (function thi$ng$geom$viz$core$log_ticks_domain(base,d1,d2){
var log_STAR_ = thi.ng.geom.viz.core.log.call(null,base);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.math.core.floor.call(null,log_STAR_.call(null,d1)),thi.ng.math.core.ceil.call(null,log_STAR_.call(null,d2))], null);
});
thi.ng.geom.viz.core.log_tick_marks_major = (function thi$ng$geom$viz$core$log_tick_marks_major(base,p__32658){
var vec__32665 = p__32658;
var d1 = cljs.core.nth.call(null,vec__32665,(0),null);
var d2 = cljs.core.nth.call(null,vec__32665,(1),null);
var vec__32666 = thi.ng.geom.viz.core.log_ticks_domain.call(null,base,d1,d2);
var d1l = cljs.core.nth.call(null,vec__32666,(0),null);
var d2l = cljs.core.nth.call(null,vec__32666,(1),null);
return cljs.core.filter.call(null,((function (vec__32666,d1l,d2l,vec__32665,d1,d2){
return (function (p1__32657_SHARP_){
return thi.ng.math.core.in_range_QMARK_.call(null,d1,d2,p1__32657_SHARP_);
});})(vec__32666,d1l,d2l,vec__32665,d1,d2))
,(function (){var iter__16823__auto__ = ((function (vec__32666,d1l,d2l,vec__32665,d1,d2){
return (function thi$ng$geom$viz$core$log_tick_marks_major_$_iter__32667(s__32668){
return (new cljs.core.LazySeq(null,((function (vec__32666,d1l,d2l,vec__32665,d1,d2){
return (function (){
var s__32668__$1 = s__32668;
while(true){
var temp__4425__auto__ = cljs.core.seq.call(null,s__32668__$1);
if(temp__4425__auto__){
var s__32668__$2 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,s__32668__$2)){
var c__16821__auto__ = cljs.core.chunk_first.call(null,s__32668__$2);
var size__16822__auto__ = cljs.core.count.call(null,c__16821__auto__);
var b__32670 = cljs.core.chunk_buffer.call(null,size__16822__auto__);
if((function (){var i__32669 = (0);
while(true){
if((i__32669 < size__16822__auto__)){
var i = cljs.core._nth.call(null,c__16821__auto__,i__32669);
cljs.core.chunk_append.call(null,b__32670,(((i >= (0)))?(((1) / base) * Math.pow(base,i)):(((1) / base) * (- Math.pow(base,(- i))))));

var G__32671 = (i__32669 + (1));
i__32669 = G__32671;
continue;
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32670),thi$ng$geom$viz$core$log_tick_marks_major_$_iter__32667.call(null,cljs.core.chunk_rest.call(null,s__32668__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32670),null);
}
} else {
var i = cljs.core.first.call(null,s__32668__$2);
return cljs.core.cons.call(null,(((i >= (0)))?(((1) / base) * Math.pow(base,i)):(((1) / base) * (- Math.pow(base,(- i))))),thi$ng$geom$viz$core$log_tick_marks_major_$_iter__32667.call(null,cljs.core.rest.call(null,s__32668__$2)));
}
} else {
return null;
}
break;
}
});})(vec__32666,d1l,d2l,vec__32665,d1,d2))
,null,null));
});})(vec__32666,d1l,d2l,vec__32665,d1,d2))
;
return iter__16823__auto__.call(null,cljs.core.range.call(null,d1l,(d2l + (1))));
})());
});
thi.ng.geom.viz.core.log_tick_marks_minor = (function thi$ng$geom$viz$core$log_tick_marks_minor(base,p__32673){
var vec__32682 = p__32673;
var d1 = cljs.core.nth.call(null,vec__32682,(0),null);
var d2 = cljs.core.nth.call(null,vec__32682,(1),null);
var vec__32683 = thi.ng.geom.viz.core.log_ticks_domain.call(null,base,d1,d2);
var d1l = cljs.core.nth.call(null,vec__32683,(0),null);
var d2l = cljs.core.nth.call(null,vec__32683,(1),null);
var ticks = ((((2) === base))?new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [0.75], null):cljs.core.range.call(null,(2),base));
return cljs.core.filter.call(null,((function (vec__32683,d1l,d2l,ticks,vec__32682,d1,d2){
return (function (p1__32672_SHARP_){
return thi.ng.math.core.in_range_QMARK_.call(null,d1,d2,p1__32672_SHARP_);
});})(vec__32683,d1l,d2l,ticks,vec__32682,d1,d2))
,(function (){var iter__16823__auto__ = ((function (vec__32683,d1l,d2l,ticks,vec__32682,d1,d2){
return (function thi$ng$geom$viz$core$log_tick_marks_minor_$_iter__32684(s__32685){
return (new cljs.core.LazySeq(null,((function (vec__32683,d1l,d2l,ticks,vec__32682,d1,d2){
return (function (){
var s__32685__$1 = s__32685;
while(true){
var temp__4425__auto__ = cljs.core.seq.call(null,s__32685__$1);
if(temp__4425__auto__){
var xs__4977__auto__ = temp__4425__auto__;
var i = cljs.core.first.call(null,xs__4977__auto__);
var iterys__16819__auto__ = ((function (s__32685__$1,i,xs__4977__auto__,temp__4425__auto__,vec__32683,d1l,d2l,ticks,vec__32682,d1,d2){
return (function thi$ng$geom$viz$core$log_tick_marks_minor_$_iter__32684_$_iter__32686(s__32687){
return (new cljs.core.LazySeq(null,((function (s__32685__$1,i,xs__4977__auto__,temp__4425__auto__,vec__32683,d1l,d2l,ticks,vec__32682,d1,d2){
return (function (){
var s__32687__$1 = s__32687;
while(true){
var temp__4425__auto____$1 = cljs.core.seq.call(null,s__32687__$1);
if(temp__4425__auto____$1){
var s__32687__$2 = temp__4425__auto____$1;
if(cljs.core.chunked_seq_QMARK_.call(null,s__32687__$2)){
var c__16821__auto__ = cljs.core.chunk_first.call(null,s__32687__$2);
var size__16822__auto__ = cljs.core.count.call(null,c__16821__auto__);
var b__32689 = cljs.core.chunk_buffer.call(null,size__16822__auto__);
if((function (){var i__32688 = (0);
while(true){
if((i__32688 < size__16822__auto__)){
var j = cljs.core._nth.call(null,c__16821__auto__,i__32688);
cljs.core.chunk_append.call(null,b__32689,(((i >= (0)))?((j / base) * Math.pow(base,i)):((j / base) * (- Math.pow(base,(- i))))));

var G__32690 = (i__32688 + (1));
i__32688 = G__32690;
continue;
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32689),thi$ng$geom$viz$core$log_tick_marks_minor_$_iter__32684_$_iter__32686.call(null,cljs.core.chunk_rest.call(null,s__32687__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32689),null);
}
} else {
var j = cljs.core.first.call(null,s__32687__$2);
return cljs.core.cons.call(null,(((i >= (0)))?((j / base) * Math.pow(base,i)):((j / base) * (- Math.pow(base,(- i))))),thi$ng$geom$viz$core$log_tick_marks_minor_$_iter__32684_$_iter__32686.call(null,cljs.core.rest.call(null,s__32687__$2)));
}
} else {
return null;
}
break;
}
});})(s__32685__$1,i,xs__4977__auto__,temp__4425__auto__,vec__32683,d1l,d2l,ticks,vec__32682,d1,d2))
,null,null));
});})(s__32685__$1,i,xs__4977__auto__,temp__4425__auto__,vec__32683,d1l,d2l,ticks,vec__32682,d1,d2))
;
var fs__16820__auto__ = cljs.core.seq.call(null,iterys__16819__auto__.call(null,ticks));
if(fs__16820__auto__){
return cljs.core.concat.call(null,fs__16820__auto__,thi$ng$geom$viz$core$log_tick_marks_minor_$_iter__32684.call(null,cljs.core.rest.call(null,s__32685__$1)));
} else {
var G__32691 = cljs.core.rest.call(null,s__32685__$1);
s__32685__$1 = G__32691;
continue;
}
} else {
return null;
}
break;
}
});})(vec__32683,d1l,d2l,ticks,vec__32682,d1,d2))
,null,null));
});})(vec__32683,d1l,d2l,ticks,vec__32682,d1,d2))
;
return iter__16823__auto__.call(null,cljs.core.range.call(null,d1l,(d2l + (1))));
})());
});
thi.ng.geom.viz.core.log_axis = (function thi$ng$geom$viz$core$log_axis(p__32692){
var map__32694 = p__32692;
var map__32694__$1 = ((cljs.core.seq_QMARK_.call(null,map__32694))?cljs.core.apply.call(null,cljs.core.hash_map,map__32694):map__32694);
var spec = map__32694__$1;
var base = cljs.core.get.call(null,map__32694__$1,new cljs.core.Keyword(null,"base","base",185279322),(10));
var domain = cljs.core.get.call(null,map__32694__$1,new cljs.core.Keyword(null,"domain","domain",1847214937));
var range = cljs.core.get.call(null,map__32694__$1,new cljs.core.Keyword(null,"range","range",1639692286));
return thi.ng.geom.viz.core.axis_common_STAR_.call(null,cljs.core.assoc.call(null,spec,new cljs.core.Keyword(null,"scale","scale",-230427353),thi.ng.geom.viz.core.log_scale.call(null,base,domain,range),new cljs.core.Keyword(null,"major","major",-27376078),thi.ng.geom.viz.core.log_tick_marks_major.call(null,base,domain),new cljs.core.Keyword(null,"minor","minor",-608536071),thi.ng.geom.viz.core.log_tick_marks_minor.call(null,base,domain)));
});
thi.ng.geom.viz.core.lens_axis = (function thi$ng$geom$viz$core$lens_axis(p__32695){
var map__32697 = p__32695;
var map__32697__$1 = ((cljs.core.seq_QMARK_.call(null,map__32697))?cljs.core.apply.call(null,cljs.core.hash_map,map__32697):map__32697);
var spec = map__32697__$1;
var domain = cljs.core.get.call(null,map__32697__$1,new cljs.core.Keyword(null,"domain","domain",1847214937));
var range = cljs.core.get.call(null,map__32697__$1,new cljs.core.Keyword(null,"range","range",1639692286));
var focus = cljs.core.get.call(null,map__32697__$1,new cljs.core.Keyword(null,"focus","focus",234677911));
var strength = cljs.core.get.call(null,map__32697__$1,new cljs.core.Keyword(null,"strength","strength",-415606478),0.5);
var major = cljs.core.get.call(null,map__32697__$1,new cljs.core.Keyword(null,"major","major",-27376078));
var minor = cljs.core.get.call(null,map__32697__$1,new cljs.core.Keyword(null,"minor","minor",-608536071));
var major_SINGLEQUOTE_ = (cljs.core.truth_(major)?thi.ng.geom.viz.core.lin_tick_marks.call(null,domain,major):null);
var minor_SINGLEQUOTE_ = (cljs.core.truth_(minor)?thi.ng.geom.viz.core.lin_tick_marks.call(null,domain,minor):null);
var minor_SINGLEQUOTE___$1 = (cljs.core.truth_((function (){var and__16057__auto__ = major_SINGLEQUOTE_;
if(cljs.core.truth_(and__16057__auto__)){
return minor_SINGLEQUOTE_;
} else {
return and__16057__auto__;
}
})())?cljs.core.filter.call(null,cljs.core.complement.call(null,cljs.core.set.call(null,major_SINGLEQUOTE_)),minor_SINGLEQUOTE_):minor_SINGLEQUOTE_);
var focus__$1 = (function (){var or__16069__auto__ = focus;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return (cljs.core.apply.call(null,cljs.core._PLUS_,domain) / 2.0);
}
})();
return thi.ng.geom.viz.core.axis_common_STAR_.call(null,cljs.core.assoc.call(null,spec,new cljs.core.Keyword(null,"scale","scale",-230427353),thi.ng.geom.viz.core.lens_scale.call(null,focus__$1,strength,domain,range),new cljs.core.Keyword(null,"major","major",-27376078),major_SINGLEQUOTE_,new cljs.core.Keyword(null,"minor","minor",-608536071),minor_SINGLEQUOTE___$1,new cljs.core.Keyword(null,"focus","focus",234677911),focus__$1,new cljs.core.Keyword(null,"strength","strength",-415606478),strength));
});
thi.ng.geom.viz.core.svg_triangle_up = (function thi$ng$geom$viz$core$svg_triangle_up(w){
var h = (w * Math.sin(thi.ng.math.core.THIRD_PI));
var w__$1 = (0.5 * w);
return ((function (h,w__$1){
return (function (p__32701){
var vec__32702 = p__32701;
var vec__32703 = cljs.core.nth.call(null,vec__32702,(0),null);
var x = cljs.core.nth.call(null,vec__32703,(0),null);
var y = cljs.core.nth.call(null,vec__32703,(1),null);
return thi.ng.geom.svg.core.polygon.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(x - w__$1),(y + h)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(x + w__$1),(y + h)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null)], null));
});
;})(h,w__$1))
});
thi.ng.geom.viz.core.svg_triangle_down = (function thi$ng$geom$viz$core$svg_triangle_down(w){
var h = (w * Math.sin(thi.ng.math.core.THIRD_PI));
var w__$1 = (0.5 * w);
return ((function (h,w__$1){
return (function (p__32707){
var vec__32708 = p__32707;
var vec__32709 = cljs.core.nth.call(null,vec__32708,(0),null);
var x = cljs.core.nth.call(null,vec__32709,(0),null);
var y = cljs.core.nth.call(null,vec__32709,(1),null);
return thi.ng.geom.svg.core.polygon.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(x - w__$1),(y - h)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(x + w__$1),(y - h)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null)], null));
});
;})(h,w__$1))
});
thi.ng.geom.viz.core.svg_square = (function thi$ng$geom$viz$core$svg_square(r){
var d = (r * 2.0);
return ((function (d){
return (function (p__32713){
var vec__32714 = p__32713;
var vec__32715 = cljs.core.nth.call(null,vec__32714,(0),null);
var x = cljs.core.nth.call(null,vec__32715,(0),null);
var y = cljs.core.nth.call(null,vec__32715,(1),null);
return thi.ng.geom.svg.core.rect.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(x - r),(y - r)], null),d,d);
});
;})(d))
});
thi.ng.geom.viz.core.labeled_rect_horizontal = (function thi$ng$geom$viz$core$labeled_rect_horizontal(p__32716){
var map__32722 = p__32716;
var map__32722__$1 = ((cljs.core.seq_QMARK_.call(null,map__32722))?cljs.core.apply.call(null,cljs.core.hash_map,map__32722):map__32722);
var h = cljs.core.get.call(null,map__32722__$1,new cljs.core.Keyword(null,"h","h",1109658740));
var r = cljs.core.get.call(null,map__32722__$1,new cljs.core.Keyword(null,"r","r",-471384190));
var label = cljs.core.get.call(null,map__32722__$1,new cljs.core.Keyword(null,"label","label",1718410804));
var fill = cljs.core.get.call(null,map__32722__$1,new cljs.core.Keyword(null,"fill","fill",883462889));
var min_width = cljs.core.get.call(null,map__32722__$1,new cljs.core.Keyword(null,"min-width","min-width",1926193728));
var base_line = cljs.core.get.call(null,map__32722__$1,new cljs.core.Keyword(null,"base-line","base-line",577717338));
var r2 = ((-2) * r);
var h2 = (0.5 * h);
return ((function (r2,h2,map__32722,map__32722__$1,h,r,label,fill,min_width,base_line){
return (function (p__32723){
var vec__32724 = p__32723;
var vec__32725 = cljs.core.nth.call(null,vec__32724,(0),null);
var ax = cljs.core.nth.call(null,vec__32725,(0),null);
var ay = cljs.core.nth.call(null,vec__32725,(1),null);
var a = vec__32725;
var vec__32726 = cljs.core.nth.call(null,vec__32724,(1),null);
var bx = cljs.core.nth.call(null,vec__32726,(0),null);
var b = vec__32726;
var item = cljs.core.nth.call(null,vec__32724,(2),null);
return thi.ng.geom.svg.core.group.call(null,cljs.core.PersistentArrayMap.EMPTY,thi.ng.geom.svg.core.rect.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(ax - r),(ay - h2)], null),((bx - ax) - r2),h,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"fill","fill",883462889),fill.call(null,item),new cljs.core.Keyword(null,"rx","rx",1627208482),r,new cljs.core.Keyword(null,"ry","ry",-334598563),r], null)),(((min_width < (bx - ax)))?thi.ng.geom.svg.core.text.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [ax,(base_line + ay)], null),label.call(null,item)):null));
});
;})(r2,h2,map__32722,map__32722__$1,h,r,label,fill,min_width,base_line))
});
thi.ng.geom.viz.core.svg_line_plot = (function thi$ng$geom$viz$core$svg_line_plot(v_spec,d_spec){
return thi.ng.geom.svg.core.line_strip.call(null,cljs.core.map.call(null,cljs.core.first,thi.ng.geom.viz.core.process_points.call(null,v_spec,d_spec)),new cljs.core.Keyword(null,"attribs","attribs",-137878093).cljs$core$IFn$_invoke$arity$1(d_spec));
});
thi.ng.geom.viz.core.svg_area_plot = (function thi$ng$geom$viz$core$svg_area_plot(p__32727,p__32728){
var map__32731 = p__32727;
var map__32731__$1 = ((cljs.core.seq_QMARK_.call(null,map__32731))?cljs.core.apply.call(null,cljs.core.hash_map,map__32731):map__32731);
var v_spec = map__32731__$1;
var y_axis = cljs.core.get.call(null,map__32731__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var project = cljs.core.get.call(null,map__32731__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var map__32732 = p__32728;
var map__32732__$1 = ((cljs.core.seq_QMARK_.call(null,map__32732))?cljs.core.apply.call(null,cljs.core.hash_map,map__32732):map__32732);
var d_spec = map__32732__$1;
var res = cljs.core.get.call(null,map__32732__$1,new cljs.core.Keyword(null,"res","res",-1395007879));
var ry1 = cljs.core.first.call(null,new cljs.core.Keyword(null,"range","range",1639692286).cljs$core$IFn$_invoke$arity$1(y_axis));
var points = cljs.core.map.call(null,cljs.core.first,thi.ng.geom.viz.core.process_points.call(null,cljs.core.assoc.call(null,v_spec,new cljs.core.Keyword(null,"project","project",1124394579),thi.ng.geom.core.vector.vec2),d_spec));
var p = thi.ng.geom.core.vector.vec2.call(null,cljs.core.first.call(null,cljs.core.last.call(null,points)),ry1);
var q = thi.ng.geom.core.vector.vec2.call(null,cljs.core.ffirst.call(null,points),ry1);
var points__$1 = cljs.core.concat.call(null,points,cljs.core.map.call(null,cljs.core.partial.call(null,thi.ng.geom.core.mix,p,q),thi.ng.math.core.norm_range.call(null,(function (){var or__16069__auto__ = res;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return (1);
}
})())));
return thi.ng.geom.svg.core.polygon.call(null,cljs.core.map.call(null,project,points__$1),new cljs.core.Keyword(null,"attribs","attribs",-137878093).cljs$core$IFn$_invoke$arity$1(d_spec));
});
thi.ng.geom.viz.core.svg_radar_plot = (function thi$ng$geom$viz$core$svg_radar_plot(v_spec,p__32733){
var map__32735 = p__32733;
var map__32735__$1 = ((cljs.core.seq_QMARK_.call(null,map__32735))?cljs.core.apply.call(null,cljs.core.hash_map,map__32735):map__32735);
var d_spec = map__32735__$1;
var shape = cljs.core.get.call(null,map__32735__$1,new cljs.core.Keyword(null,"shape","shape",1190694006),thi.ng.geom.svg.core.polygon);
return shape.call(null,cljs.core.map.call(null,cljs.core.first,thi.ng.geom.viz.core.process_points.call(null,v_spec,d_spec)),new cljs.core.Keyword(null,"attribs","attribs",-137878093).cljs$core$IFn$_invoke$arity$1(d_spec));
});
thi.ng.geom.viz.core.svg_radar_plot_minmax = (function thi$ng$geom$viz$core$svg_radar_plot_minmax(v_spec,p__32739){
var map__32741 = p__32739;
var map__32741__$1 = ((cljs.core.seq_QMARK_.call(null,map__32741))?cljs.core.apply.call(null,cljs.core.hash_map,map__32741):map__32741);
var d_spec = map__32741__$1;
var item_pos_min = cljs.core.get.call(null,map__32741__$1,new cljs.core.Keyword(null,"item-pos-min","item-pos-min",-938894508));
var item_pos_max = cljs.core.get.call(null,map__32741__$1,new cljs.core.Keyword(null,"item-pos-max","item-pos-max",1243635616));
var shape = cljs.core.get.call(null,map__32741__$1,new cljs.core.Keyword(null,"shape","shape",1190694006),((function (map__32741,map__32741__$1,d_spec,item_pos_min,item_pos_max){
return (function (p1__32736_SHARP_,p2__32737_SHARP_,p3__32738_SHARP_){
return thi.ng.geom.svg.core.path.call(null,cljs.core.concat.call(null,p1__32736_SHARP_,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"Z","Z",459124588)], null)], null),p2__32737_SHARP_,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"Z","Z",459124588)], null)], null)),p3__32738_SHARP_);
});})(map__32741,map__32741__$1,d_spec,item_pos_min,item_pos_max))
);
var min_points = thi.ng.geom.viz.core.points__GT_path_segments.call(null,cljs.core.map.call(null,cljs.core.first,thi.ng.geom.viz.core.process_points.call(null,v_spec,cljs.core.assoc.call(null,d_spec,new cljs.core.Keyword(null,"item-pos","item-pos",390857330),(function (){var or__16069__auto__ = item_pos_min;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return ((function (or__16069__auto__,map__32741,map__32741__$1,d_spec,item_pos_min,item_pos_max,shape){
return (function (i){
return cljs.core.take.call(null,(2),i);
});
;})(or__16069__auto__,map__32741,map__32741__$1,d_spec,item_pos_min,item_pos_max,shape))
}
})()))));
var max_points = thi.ng.geom.viz.core.points__GT_path_segments.call(null,cljs.core.map.call(null,cljs.core.first,thi.ng.geom.viz.core.process_points.call(null,v_spec,cljs.core.assoc.call(null,d_spec,new cljs.core.Keyword(null,"item-pos","item-pos",390857330),(function (){var or__16069__auto__ = item_pos_max;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return ((function (or__16069__auto__,min_points,map__32741,map__32741__$1,d_spec,item_pos_min,item_pos_max,shape){
return (function (i){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.first.call(null,i),cljs.core.nth.call(null,i,(2))], null);
});
;})(or__16069__auto__,min_points,map__32741,map__32741__$1,d_spec,item_pos_min,item_pos_max,shape))
}
})()))));
return shape.call(null,max_points,min_points,cljs.core.assoc.call(null,new cljs.core.Keyword(null,"attribs","attribs",-137878093).cljs$core$IFn$_invoke$arity$1(d_spec),new cljs.core.Keyword(null,"fill-rule","fill-rule",-1824841598),"evenodd"));
});
thi.ng.geom.viz.core.svg_scatter_plot = (function thi$ng$geom$viz$core$svg_scatter_plot(v_spec,p__32742){
var map__32746 = p__32742;
var map__32746__$1 = ((cljs.core.seq_QMARK_.call(null,map__32746))?cljs.core.apply.call(null,cljs.core.hash_map,map__32746):map__32746);
var d_spec = map__32746__$1;
var attribs = cljs.core.get.call(null,map__32746__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var shape = cljs.core.get.call(null,map__32746__$1,new cljs.core.Keyword(null,"shape","shape",1190694006));
return cljs.core.apply.call(null,thi.ng.geom.svg.core.group,attribs,thi.ng.geom.viz.core.process_points.call(null,v_spec,cljs.core.assoc.call(null,d_spec,new cljs.core.Keyword(null,"shape","shape",1190694006),(function (){var or__16069__auto__ = shape;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return ((function (or__16069__auto__,map__32746,map__32746__$1,d_spec,attribs,shape){
return (function (p__32747){
var vec__32748 = p__32747;
var p = cljs.core.nth.call(null,vec__32748,(0),null);
return thi.ng.geom.svg.core.circle.call(null,p,(3));
});
;})(or__16069__auto__,map__32746,map__32746__$1,d_spec,attribs,shape))
}
})())));
});
thi.ng.geom.viz.core.svg_bar_plot = (function thi$ng$geom$viz$core$svg_bar_plot(p__32750,p__32751){
var map__32757 = p__32750;
var map__32757__$1 = ((cljs.core.seq_QMARK_.call(null,map__32757))?cljs.core.apply.call(null,cljs.core.hash_map,map__32757):map__32757);
var x_axis = cljs.core.get.call(null,map__32757__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32757__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var project = cljs.core.get.call(null,map__32757__$1,new cljs.core.Keyword(null,"project","project",1124394579),thi.ng.geom.core.vector.vec2);
var map__32758 = p__32751;
var map__32758__$1 = ((cljs.core.seq_QMARK_.call(null,map__32758))?cljs.core.apply.call(null,cljs.core.hash_map,map__32758):map__32758);
var values = cljs.core.get.call(null,map__32758__$1,new cljs.core.Keyword(null,"values","values",372645556));
var attribs = cljs.core.get.call(null,map__32758__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var shape = cljs.core.get.call(null,map__32758__$1,new cljs.core.Keyword(null,"shape","shape",1190694006),((function (map__32757,map__32757__$1,x_axis,y_axis,project,map__32758,map__32758__$1,values,attribs){
return (function (a,b,_){
return thi.ng.geom.svg.core.line.call(null,a,b);
});})(map__32757,map__32757__$1,x_axis,y_axis,project,map__32758,map__32758__$1,values,attribs))
);
var item_pos = cljs.core.get.call(null,map__32758__$1,new cljs.core.Keyword(null,"item-pos","item-pos",390857330),cljs.core.identity);
var interleave = cljs.core.get.call(null,map__32758__$1,new cljs.core.Keyword(null,"interleave","interleave",-1475043421),(1));
var offset = cljs.core.get.call(null,map__32758__$1,new cljs.core.Keyword(null,"offset","offset",296498311),(0));
var bar_width = cljs.core.get.call(null,map__32758__$1,new cljs.core.Keyword(null,"bar-width","bar-width",1233240523),(0));
var domain = new cljs.core.Keyword(null,"domain","domain",1847214937).cljs$core$IFn$_invoke$arity$1(x_axis);
var base_y = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis).call(null,cljs.core.first.call(null,new cljs.core.Keyword(null,"domain","domain",1847214937).cljs$core$IFn$_invoke$arity$1(y_axis)));
var mapper = thi.ng.geom.viz.core.value_mapper.call(null,new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(x_axis),new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis));
var offset__$1 = ((-0.5 * (interleave * bar_width)) + ((offset + 0.5) * bar_width));
return thi.ng.geom.svg.core.group.call(null,attribs,cljs.core.sequence.call(null,cljs.core.comp.call(null,cljs.core.map.call(null,cljs.core.juxt.call(null,item_pos,cljs.core.identity)),cljs.core.filter.call(null,((function (domain,base_y,mapper,offset__$1,map__32757,map__32757__$1,x_axis,y_axis,project,map__32758,map__32758__$1,values,attribs,shape,item_pos,interleave,offset,bar_width){
return (function (p1__32749_SHARP_){
return thi.ng.math.core.in_range_QMARK_.call(null,domain,cljs.core.ffirst.call(null,p1__32749_SHARP_));
});})(domain,base_y,mapper,offset__$1,map__32757,map__32757__$1,x_axis,y_axis,project,map__32758,map__32758__$1,values,attribs,shape,item_pos,interleave,offset,bar_width))
),cljs.core.map.call(null,((function (domain,base_y,mapper,offset__$1,map__32757,map__32757__$1,x_axis,y_axis,project,map__32758,map__32758__$1,values,attribs,shape,item_pos,interleave,offset,bar_width){
return (function (p__32759){
var vec__32760 = p__32759;
var p = cljs.core.nth.call(null,vec__32760,(0),null);
var i = cljs.core.nth.call(null,vec__32760,(1),null);
var vec__32761 = mapper.call(null,p);
var ax = cljs.core.nth.call(null,vec__32761,(0),null);
var ay = cljs.core.nth.call(null,vec__32761,(1),null);
var ax__$1 = (ax + offset__$1);
return shape.call(null,project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [ax__$1,ay], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [ax__$1,base_y], null)),i);
});})(domain,base_y,mapper,offset__$1,map__32757,map__32757__$1,x_axis,y_axis,project,map__32758,map__32758__$1,values,attribs,shape,item_pos,interleave,offset,bar_width))
)),values));
});
thi.ng.geom.viz.core.svg_heatmap = (function thi$ng$geom$viz$core$svg_heatmap(p__32767,p__32768){
var map__32779 = p__32767;
var map__32779__$1 = ((cljs.core.seq_QMARK_.call(null,map__32779))?cljs.core.apply.call(null,cljs.core.hash_map,map__32779):map__32779);
var x_axis = cljs.core.get.call(null,map__32779__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32779__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var project = cljs.core.get.call(null,map__32779__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var map__32780 = p__32768;
var map__32780__$1 = ((cljs.core.seq_QMARK_.call(null,map__32780))?cljs.core.apply.call(null,cljs.core.hash_map,map__32780):map__32780);
var d_spec = map__32780__$1;
var matrix = cljs.core.get.call(null,map__32780__$1,new cljs.core.Keyword(null,"matrix","matrix",803137200));
var value_domain = cljs.core.get.call(null,map__32780__$1,new cljs.core.Keyword(null,"value-domain","value-domain",1224230851),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [0.0,1.0], null));
var clamp = cljs.core.get.call(null,map__32780__$1,new cljs.core.Keyword(null,"clamp","clamp",1803814940));
var palette = cljs.core.get.call(null,map__32780__$1,new cljs.core.Keyword(null,"palette","palette",-456203511));
var palette_scale = cljs.core.get.call(null,map__32780__$1,new cljs.core.Keyword(null,"palette-scale","palette-scale",2003276610),thi.ng.geom.viz.core.linear_scale);
var attribs = cljs.core.get.call(null,map__32780__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var shape = cljs.core.get.call(null,map__32780__$1,new cljs.core.Keyword(null,"shape","shape",1190694006),((function (map__32779,map__32779__$1,x_axis,y_axis,project,map__32780,map__32780__$1,d_spec,matrix,value_domain,clamp,palette,palette_scale,attribs){
return (function (p1__32762_SHARP_,p2__32763_SHARP_,p3__32764_SHARP_,p4__32765_SHARP_,p5__32766_SHARP_){
return thi.ng.geom.svg.core.polygon.call(null,new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [p1__32762_SHARP_,p2__32763_SHARP_,p3__32764_SHARP_,p4__32765_SHARP_], null),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"fill","fill",883462889),p5__32766_SHARP_], null));
});})(map__32779,map__32779__$1,x_axis,y_axis,project,map__32780,map__32780__$1,d_spec,matrix,value_domain,clamp,palette,palette_scale,attribs))
);
var scale_x = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(x_axis);
var scale_y = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis);
var pmax = (cljs.core.count.call(null,palette) - (1));
var scale_v = palette_scale.call(null,value_domain,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),pmax], null));
return thi.ng.geom.svg.core.group.call(null,attribs,(function (){var iter__16823__auto__ = ((function (scale_x,scale_y,pmax,scale_v,map__32779,map__32779__$1,x_axis,y_axis,project,map__32780,map__32780__$1,d_spec,matrix,value_domain,clamp,palette,palette_scale,attribs,shape){
return (function thi$ng$geom$viz$core$svg_heatmap_$_iter__32781(s__32782){
return (new cljs.core.LazySeq(null,((function (scale_x,scale_y,pmax,scale_v,map__32779,map__32779__$1,x_axis,y_axis,project,map__32780,map__32780__$1,d_spec,matrix,value_domain,clamp,palette,palette_scale,attribs,shape){
return (function (){
var s__32782__$1 = s__32782;
while(true){
var temp__4425__auto__ = cljs.core.seq.call(null,s__32782__$1);
if(temp__4425__auto__){
var s__32782__$2 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,s__32782__$2)){
var c__16821__auto__ = cljs.core.chunk_first.call(null,s__32782__$2);
var size__16822__auto__ = cljs.core.count.call(null,c__16821__auto__);
var b__32784 = cljs.core.chunk_buffer.call(null,size__16822__auto__);
if((function (){var i__32783 = (0);
while(true){
if((i__32783 < size__16822__auto__)){
var p = cljs.core._nth.call(null,c__16821__auto__,i__32783);
var vec__32787 = p;
var y = cljs.core.nth.call(null,vec__32787,(0),null);
var x = cljs.core.nth.call(null,vec__32787,(1),null);
var v = thi.ng.ndarray.core.get_at.call(null,matrix,y,x);
if(cljs.core.truth_((function (){var or__16069__auto__ = clamp;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return thi.ng.math.core.in_range_QMARK_.call(null,value_domain,v);
}
})())){
cljs.core.chunk_append.call(null,b__32784,shape.call(null,project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,x),scale_y.call(null,y)], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,(x + (1))),scale_y.call(null,y)], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,(x + (1))),scale_y.call(null,(y + (1)))], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,x),scale_y.call(null,(y + (1)))], null)),palette.call(null,thi.ng.math.core.clamp.call(null,(scale_v.call(null,v) | (0)),(0),pmax))));

var G__32789 = (i__32783 + (1));
i__32783 = G__32789;
continue;
} else {
var G__32790 = (i__32783 + (1));
i__32783 = G__32790;
continue;
}
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32784),thi$ng$geom$viz$core$svg_heatmap_$_iter__32781.call(null,cljs.core.chunk_rest.call(null,s__32782__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32784),null);
}
} else {
var p = cljs.core.first.call(null,s__32782__$2);
var vec__32788 = p;
var y = cljs.core.nth.call(null,vec__32788,(0),null);
var x = cljs.core.nth.call(null,vec__32788,(1),null);
var v = thi.ng.ndarray.core.get_at.call(null,matrix,y,x);
if(cljs.core.truth_((function (){var or__16069__auto__ = clamp;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return thi.ng.math.core.in_range_QMARK_.call(null,value_domain,v);
}
})())){
return cljs.core.cons.call(null,shape.call(null,project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,x),scale_y.call(null,y)], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,(x + (1))),scale_y.call(null,y)], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,(x + (1))),scale_y.call(null,(y + (1)))], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,x),scale_y.call(null,(y + (1)))], null)),palette.call(null,thi.ng.math.core.clamp.call(null,(scale_v.call(null,v) | (0)),(0),pmax))),thi$ng$geom$viz$core$svg_heatmap_$_iter__32781.call(null,cljs.core.rest.call(null,s__32782__$2)));
} else {
var G__32791 = cljs.core.rest.call(null,s__32782__$2);
s__32782__$1 = G__32791;
continue;
}
}
} else {
return null;
}
break;
}
});})(scale_x,scale_y,pmax,scale_v,map__32779,map__32779__$1,x_axis,y_axis,project,map__32780,map__32780__$1,d_spec,matrix,value_domain,clamp,palette,palette_scale,attribs,shape))
,null,null));
});})(scale_x,scale_y,pmax,scale_v,map__32779,map__32779__$1,x_axis,y_axis,project,map__32780,map__32780__$1,d_spec,matrix,value_domain,clamp,palette,palette_scale,attribs,shape))
;
return iter__16823__auto__.call(null,thi.ng.ndarray.core.position_seq.call(null,matrix));
})());
});
thi.ng.geom.viz.core.matrix_2d = (function thi$ng$geom$viz$core$matrix_2d(w,h,values){
return thi.ng.ndarray.core.ndarray.call(null,new cljs.core.Keyword(null,"float32","float32",-2119815775),values,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [h,w], null));
});
thi.ng.geom.viz.core.contour_matrix = (function thi$ng$geom$viz$core$contour_matrix(w,h,values){
return thi.ng.ndarray.contours.set_border_2d.call(null,thi.ng.geom.viz.core.matrix_2d.call(null,w,h,values),-1.0E9);
});
thi.ng.geom.viz.core.contour__GT_svg = (function thi$ng$geom$viz$core$contour__GT_svg(scale_x,scale_y,project){
return (function (attribs,contour){
var contour__$1 = cljs.core.map.call(null,(function (p__32794){
var vec__32795 = p__32794;
var y = cljs.core.nth.call(null,vec__32795,(0),null);
var x = cljs.core.nth.call(null,vec__32795,(1),null);
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [scale_x.call(null,x),scale_y.call(null,y)], null);
}),contour);
return thi.ng.geom.svg.core.polygon.call(null,cljs.core.map.call(null,project,contour__$1),attribs);
});
});
thi.ng.geom.viz.core.svg_contour_plot = (function thi$ng$geom$viz$core$svg_contour_plot(p__32796,p__32797){
var map__32800 = p__32796;
var map__32800__$1 = ((cljs.core.seq_QMARK_.call(null,map__32800))?cljs.core.apply.call(null,cljs.core.hash_map,map__32800):map__32800);
var x_axis = cljs.core.get.call(null,map__32800__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32800__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var project = cljs.core.get.call(null,map__32800__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var map__32801 = p__32797;
var map__32801__$1 = ((cljs.core.seq_QMARK_.call(null,map__32801))?cljs.core.apply.call(null,cljs.core.hash_map,map__32801):map__32801);
var matrix = cljs.core.get.call(null,map__32801__$1,new cljs.core.Keyword(null,"matrix","matrix",803137200));
var attribs = cljs.core.get.call(null,map__32801__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var levels = cljs.core.get.call(null,map__32801__$1,new cljs.core.Keyword(null,"levels","levels",-950747887));
var palette = cljs.core.get.call(null,map__32801__$1,new cljs.core.Keyword(null,"palette","palette",-456203511),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1),(1),(1)], null)], null));
var palette_scale = cljs.core.get.call(null,map__32801__$1,new cljs.core.Keyword(null,"palette-scale","palette-scale",2003276610),thi.ng.geom.viz.core.linear_scale);
var value_domain = cljs.core.get.call(null,map__32801__$1,new cljs.core.Keyword(null,"value-domain","value-domain",1224230851),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [0.0,1.0], null));
var contour_attribs = cljs.core.get.call(null,map__32801__$1,new cljs.core.Keyword(null,"contour-attribs","contour-attribs",464584885),cljs.core.constantly.call(null,null));
var pmax = (cljs.core.count.call(null,palette) - (1));
var scale_v = palette_scale.call(null,value_domain,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),pmax], null));
var contour_fn = thi.ng.geom.viz.core.contour__GT_svg.call(null,new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(x_axis),new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis),project);
return thi.ng.geom.svg.core.group.call(null,attribs,cljs.core.map.call(null,((function (pmax,scale_v,contour_fn,map__32800,map__32800__$1,x_axis,y_axis,project,map__32801,map__32801__$1,matrix,attribs,levels,palette,palette_scale,value_domain,contour_attribs){
return (function (iso){
var c_attribs = contour_attribs.call(null,palette.call(null,thi.ng.math.core.clamp.call(null,(scale_v.call(null,iso) | (0)),(0),pmax)));
return thi.ng.geom.svg.core.group.call(null,cljs.core.PersistentArrayMap.EMPTY,cljs.core.map.call(null,cljs.core.partial.call(null,contour_fn,c_attribs),thi.ng.ndarray.contours.find_contours_2d.call(null,matrix,iso)));
});})(pmax,scale_v,contour_fn,map__32800,map__32800__$1,x_axis,y_axis,project,map__32801,map__32801__$1,matrix,attribs,levels,palette,palette_scale,value_domain,contour_attribs))
,cljs.core.sort.call(null,levels)));
});
thi.ng.geom.viz.core.overlap_QMARK_ = (function thi$ng$geom$viz$core$overlap_QMARK_(p__32802,p__32803){
var vec__32806 = p__32802;
var a = cljs.core.nth.call(null,vec__32806,(0),null);
var b = cljs.core.nth.call(null,vec__32806,(1),null);
var vec__32807 = p__32803;
var c = cljs.core.nth.call(null,vec__32807,(0),null);
var d = cljs.core.nth.call(null,vec__32807,(1),null);
return ((a <= d)) && ((b >= c));
});
thi.ng.geom.viz.core.compute_row_stacking = (function thi$ng$geom$viz$core$compute_row_stacking(item_range,coll){
return cljs.core.reduce.call(null,(function (grid,x){
var r = item_range.call(null,x);
var G__32815 = grid;
var vec__32816 = G__32815;
var row = cljs.core.nth.call(null,vec__32816,(0),null);
var more = cljs.core.nthnext.call(null,vec__32816,(1));
var idx = (0);
var G__32815__$1 = G__32815;
var idx__$1 = idx;
while(true){
var vec__32817 = G__32815__$1;
var row__$1 = cljs.core.nth.call(null,vec__32817,(0),null);
var more__$1 = cljs.core.nthnext.call(null,vec__32817,(1));
var idx__$2 = idx__$1;
if(((row__$1 == null)) || (cljs.core.not.call(null,cljs.core.some.call(null,((function (G__32815__$1,idx__$1,vec__32817,row__$1,more__$1,idx__$2,G__32815,vec__32816,row,more,idx,r){
return (function (p1__32808_SHARP_){
return thi.ng.geom.viz.core.overlap_QMARK_.call(null,r,item_range.call(null,p1__32808_SHARP_));
});})(G__32815__$1,idx__$1,vec__32817,row__$1,more__$1,idx__$2,G__32815,vec__32816,row,more,idx,r))
,row__$1)))){
return cljs.core.update_in.call(null,grid,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [idx__$2], null),((function (G__32815__$1,idx__$1,vec__32817,row__$1,more__$1,idx__$2,G__32815,vec__32816,row,more,idx,r){
return (function (p1__32809_SHARP_){
return cljs.core.conj.call(null,(function (){var or__16069__auto__ = p1__32809_SHARP_;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return cljs.core.PersistentVector.EMPTY;
}
})(),x);
});})(G__32815__$1,idx__$1,vec__32817,row__$1,more__$1,idx__$2,G__32815,vec__32816,row,more,idx,r))
);
} else {
var G__32818 = more__$1;
var G__32819 = (idx__$2 + (1));
G__32815__$1 = G__32818;
idx__$1 = G__32819;
continue;
}
break;
}
}),cljs.core.PersistentVector.EMPTY,coll);
});
thi.ng.geom.viz.core.process_interval_row = (function thi$ng$geom$viz$core$process_interval_row(item_range,mapper,p__32820){
var vec__32823 = p__32820;
var d1 = cljs.core.nth.call(null,vec__32823,(0),null);
var d2 = cljs.core.nth.call(null,vec__32823,(1),null);
return ((function (vec__32823,d1,d2){
return (function (i,row){
return cljs.core.map.call(null,((function (vec__32823,d1,d2){
return (function (item){
var vec__32824 = item_range.call(null,item);
var a = cljs.core.nth.call(null,vec__32824,(0),null);
var b = cljs.core.nth.call(null,vec__32824,(1),null);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [mapper.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(function (){var x__16381__auto__ = d1;
var y__16382__auto__ = a;
return ((x__16381__auto__ > y__16382__auto__) ? x__16381__auto__ : y__16382__auto__);
})(),i], null)),mapper.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(function (){var x__16388__auto__ = d2;
var y__16389__auto__ = b;
return ((x__16388__auto__ < y__16389__auto__) ? x__16388__auto__ : y__16389__auto__);
})(),i], null)),item], null);
});})(vec__32823,d1,d2))
,row);
});
;})(vec__32823,d1,d2))
});
thi.ng.geom.viz.core.svg_stacked_interval_plot = (function thi$ng$geom$viz$core$svg_stacked_interval_plot(p__32826,p__32827){
var map__32832 = p__32826;
var map__32832__$1 = ((cljs.core.seq_QMARK_.call(null,map__32832))?cljs.core.apply.call(null,cljs.core.hash_map,map__32832):map__32832);
var x_axis = cljs.core.get.call(null,map__32832__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32832__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var map__32833 = p__32827;
var map__32833__$1 = ((cljs.core.seq_QMARK_.call(null,map__32833))?cljs.core.apply.call(null,cljs.core.hash_map,map__32833):map__32833);
var values = cljs.core.get.call(null,map__32833__$1,new cljs.core.Keyword(null,"values","values",372645556));
var attribs = cljs.core.get.call(null,map__32833__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var shape = cljs.core.get.call(null,map__32833__$1,new cljs.core.Keyword(null,"shape","shape",1190694006),((function (map__32832,map__32832__$1,x_axis,y_axis,map__32833,map__32833__$1,values,attribs){
return (function (p__32834){
var vec__32835 = p__32834;
var a = cljs.core.nth.call(null,vec__32835,(0),null);
var b = cljs.core.nth.call(null,vec__32835,(1),null);
return thi.ng.geom.svg.core.line.call(null,a,b);
});})(map__32832,map__32832__$1,x_axis,y_axis,map__32833,map__32833__$1,values,attribs))
);
var item_range = cljs.core.get.call(null,map__32833__$1,new cljs.core.Keyword(null,"item-range","item-range",1733769894),cljs.core.identity);
var offset = cljs.core.get.call(null,map__32833__$1,new cljs.core.Keyword(null,"offset","offset",296498311),(0));
var scale_x = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(x_axis);
var scale_y = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis);
var domain = new cljs.core.Keyword(null,"domain","domain",1847214937).cljs$core$IFn$_invoke$arity$1(x_axis);
var mapper = thi.ng.geom.viz.core.value_mapper.call(null,scale_x,scale_y);
return thi.ng.geom.svg.core.group.call(null,attribs,cljs.core.map.call(null,shape,cljs.core.mapcat.call(null,thi.ng.geom.viz.core.process_interval_row.call(null,item_range,mapper,domain),cljs.core.range.call(null,offset,1000000.0),thi.ng.geom.viz.core.compute_row_stacking.call(null,item_range,cljs.core.sort_by.call(null,cljs.core.comp.call(null,cljs.core.first,item_range),cljs.core.filter.call(null,((function (scale_x,scale_y,domain,mapper,map__32832,map__32832__$1,x_axis,y_axis,map__32833,map__32833__$1,values,attribs,shape,item_range,offset){
return (function (p1__32825_SHARP_){
return thi.ng.geom.viz.core.overlap_QMARK_.call(null,domain,item_range.call(null,p1__32825_SHARP_));
});})(scale_x,scale_y,domain,mapper,map__32832,map__32832__$1,x_axis,y_axis,map__32833,map__32833__$1,values,attribs,shape,item_range,offset))
,values))))));
});
thi.ng.geom.viz.core.svg_axis_STAR_ = (function thi$ng$geom$viz$core$svg_axis_STAR_(p__32836,axis,tick1_fn,tick2_fn,label_fn){
var map__32838 = p__32836;
var map__32838__$1 = ((cljs.core.seq_QMARK_.call(null,map__32838))?cljs.core.apply.call(null,cljs.core.hash_map,map__32838):map__32838);
var major = cljs.core.get.call(null,map__32838__$1,new cljs.core.Keyword(null,"major","major",-27376078));
var minor = cljs.core.get.call(null,map__32838__$1,new cljs.core.Keyword(null,"minor","minor",-608536071));
var attribs = cljs.core.get.call(null,map__32838__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var label = cljs.core.get.call(null,map__32838__$1,new cljs.core.Keyword(null,"label","label",1718410804));
return thi.ng.geom.svg.core.group.call(null,cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"#000"], null),attribs),cljs.core.map.call(null,tick1_fn,major),cljs.core.map.call(null,tick2_fn,minor),thi.ng.geom.svg.core.group.call(null,cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"none"], null),label),cljs.core.map.call(null,label_fn,major)),axis);
});
thi.ng.geom.viz.core.svg_x_axis_cartesian = (function thi$ng$geom$viz$core$svg_x_axis_cartesian(p__32842){
var map__32845 = p__32842;
var map__32845__$1 = ((cljs.core.seq_QMARK_.call(null,map__32845))?cljs.core.apply.call(null,cljs.core.hash_map,map__32845):map__32845);
var spec = map__32845__$1;
var vec__32846 = cljs.core.get.call(null,map__32845__$1,new cljs.core.Keyword(null,"range","range",1639692286));
var r1 = cljs.core.nth.call(null,vec__32846,(0),null);
var r2 = cljs.core.nth.call(null,vec__32846,(1),null);
var scale = cljs.core.get.call(null,map__32845__$1,new cljs.core.Keyword(null,"scale","scale",-230427353));
var major_size = cljs.core.get.call(null,map__32845__$1,new cljs.core.Keyword(null,"major-size","major-size",-698672375));
var minor_size = cljs.core.get.call(null,map__32845__$1,new cljs.core.Keyword(null,"minor-size","minor-size",-1586355109));
var label_dist = cljs.core.get.call(null,map__32845__$1,new cljs.core.Keyword(null,"label-dist","label-dist",-538260526));
var pos = cljs.core.get.call(null,map__32845__$1,new cljs.core.Keyword(null,"pos","pos",-864607220));
var format = cljs.core.get.call(null,map__32845__$1,new cljs.core.Keyword(null,"format","format",-1306924766));
return thi.ng.geom.viz.core.svg_axis_STAR_.call(null,spec,thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [r1,pos], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [r2,pos], null)),((function (map__32845,map__32845__$1,spec,vec__32846,r1,r2,scale,major_size,minor_size,label_dist,pos,format){
return (function (p1__32839_SHARP_){
var x = scale.call(null,p1__32839_SHARP_);
return thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,pos], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,(pos + major_size)], null));
});})(map__32845,map__32845__$1,spec,vec__32846,r1,r2,scale,major_size,minor_size,label_dist,pos,format))
,((function (map__32845,map__32845__$1,spec,vec__32846,r1,r2,scale,major_size,minor_size,label_dist,pos,format){
return (function (p1__32840_SHARP_){
var x = scale.call(null,p1__32840_SHARP_);
return thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,pos], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,(pos + minor_size)], null));
});})(map__32845,map__32845__$1,spec,vec__32846,r1,r2,scale,major_size,minor_size,label_dist,pos,format))
,((function (map__32845,map__32845__$1,spec,vec__32846,r1,r2,scale,major_size,minor_size,label_dist,pos,format){
return (function (p1__32841_SHARP_){
var x = scale.call(null,p1__32841_SHARP_);
return thi.ng.geom.svg.core.text.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,(pos + label_dist)], null),format.call(null,p1__32841_SHARP_));
});})(map__32845,map__32845__$1,spec,vec__32846,r1,r2,scale,major_size,minor_size,label_dist,pos,format))
);
});
thi.ng.geom.viz.core.svg_y_axis_cartesian = (function thi$ng$geom$viz$core$svg_y_axis_cartesian(p__32850){
var map__32853 = p__32850;
var map__32853__$1 = ((cljs.core.seq_QMARK_.call(null,map__32853))?cljs.core.apply.call(null,cljs.core.hash_map,map__32853):map__32853);
var spec = map__32853__$1;
var vec__32854 = cljs.core.get.call(null,map__32853__$1,new cljs.core.Keyword(null,"range","range",1639692286));
var r1 = cljs.core.nth.call(null,vec__32854,(0),null);
var r2 = cljs.core.nth.call(null,vec__32854,(1),null);
var scale = cljs.core.get.call(null,map__32853__$1,new cljs.core.Keyword(null,"scale","scale",-230427353));
var major_size = cljs.core.get.call(null,map__32853__$1,new cljs.core.Keyword(null,"major-size","major-size",-698672375));
var minor_size = cljs.core.get.call(null,map__32853__$1,new cljs.core.Keyword(null,"minor-size","minor-size",-1586355109));
var label_dist = cljs.core.get.call(null,map__32853__$1,new cljs.core.Keyword(null,"label-dist","label-dist",-538260526));
var pos = cljs.core.get.call(null,map__32853__$1,new cljs.core.Keyword(null,"pos","pos",-864607220));
var format = cljs.core.get.call(null,map__32853__$1,new cljs.core.Keyword(null,"format","format",-1306924766));
return thi.ng.geom.viz.core.svg_axis_STAR_.call(null,spec,thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,r1], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,r2], null)),((function (map__32853,map__32853__$1,spec,vec__32854,r1,r2,scale,major_size,minor_size,label_dist,pos,format){
return (function (p1__32847_SHARP_){
var y = scale.call(null,p1__32847_SHARP_);
return thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,y], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(pos - major_size),y], null));
});})(map__32853,map__32853__$1,spec,vec__32854,r1,r2,scale,major_size,minor_size,label_dist,pos,format))
,((function (map__32853,map__32853__$1,spec,vec__32854,r1,r2,scale,major_size,minor_size,label_dist,pos,format){
return (function (p1__32848_SHARP_){
var y = scale.call(null,p1__32848_SHARP_);
return thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,y], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(pos - minor_size),y], null));
});})(map__32853,map__32853__$1,spec,vec__32854,r1,r2,scale,major_size,minor_size,label_dist,pos,format))
,((function (map__32853,map__32853__$1,spec,vec__32854,r1,r2,scale,major_size,minor_size,label_dist,pos,format){
return (function (p1__32849_SHARP_){
var y = scale.call(null,p1__32849_SHARP_);
return thi.ng.geom.svg.core.text.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(pos - label_dist),y], null),format.call(null,p1__32849_SHARP_));
});})(map__32853,map__32853__$1,spec,vec__32854,r1,r2,scale,major_size,minor_size,label_dist,pos,format))
);
});
thi.ng.geom.viz.core.select_ticks = (function thi$ng$geom$viz$core$select_ticks(axis,minor_QMARK_){
if(cljs.core.truth_(minor_QMARK_)){
return cljs.core.concat.call(null,new cljs.core.Keyword(null,"minor","minor",-608536071).cljs$core$IFn$_invoke$arity$1(axis),new cljs.core.Keyword(null,"major","major",-27376078).cljs$core$IFn$_invoke$arity$1(axis));
} else {
return new cljs.core.Keyword(null,"major","major",-27376078).cljs$core$IFn$_invoke$arity$1(axis);
}
});
thi.ng.geom.viz.core.svg_axis_grid2d_cartesian = (function thi$ng$geom$viz$core$svg_axis_grid2d_cartesian(x_axis,y_axis,p__32857){
var map__32861 = p__32857;
var map__32861__$1 = ((cljs.core.seq_QMARK_.call(null,map__32861))?cljs.core.apply.call(null,cljs.core.hash_map,map__32861):map__32861);
var attribs = cljs.core.get.call(null,map__32861__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var minor_x = cljs.core.get.call(null,map__32861__$1,new cljs.core.Keyword(null,"minor-x","minor-x",-230860299));
var minor_y = cljs.core.get.call(null,map__32861__$1,new cljs.core.Keyword(null,"minor-y","minor-y",388125550));
var vec__32862 = new cljs.core.Keyword(null,"range","range",1639692286).cljs$core$IFn$_invoke$arity$1(x_axis);
var x1 = cljs.core.nth.call(null,vec__32862,(0),null);
var x2 = cljs.core.nth.call(null,vec__32862,(1),null);
var vec__32863 = new cljs.core.Keyword(null,"range","range",1639692286).cljs$core$IFn$_invoke$arity$1(y_axis);
var y1 = cljs.core.nth.call(null,vec__32863,(0),null);
var y2 = cljs.core.nth.call(null,vec__32863,(1),null);
var scale_x = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(x_axis);
var scale_y = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis);
return thi.ng.geom.svg.core.group.call(null,cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"#ccc",new cljs.core.Keyword(null,"stroke-dasharray","stroke-dasharray",-942933855),"1 1"], null),attribs),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(x_axis))?cljs.core.map.call(null,((function (vec__32862,x1,x2,vec__32863,y1,y2,scale_x,scale_y,map__32861,map__32861__$1,attribs,minor_x,minor_y){
return (function (p1__32855_SHARP_){
var x = scale_x.call(null,p1__32855_SHARP_);
return thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y1], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y2], null));
});})(vec__32862,x1,x2,vec__32863,y1,y2,scale_x,scale_y,map__32861,map__32861__$1,attribs,minor_x,minor_y))
,thi.ng.geom.viz.core.select_ticks.call(null,x_axis,minor_x)):null),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(y_axis))?cljs.core.map.call(null,((function (vec__32862,x1,x2,vec__32863,y1,y2,scale_x,scale_y,map__32861,map__32861__$1,attribs,minor_x,minor_y){
return (function (p1__32856_SHARP_){
var y = scale_y.call(null,p1__32856_SHARP_);
return thi.ng.geom.svg.core.line.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x1,y], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x2,y], null));
});})(vec__32862,x1,x2,vec__32863,y1,y2,scale_x,scale_y,map__32861,map__32861__$1,attribs,minor_x,minor_y))
,thi.ng.geom.viz.core.select_ticks.call(null,y_axis,minor_y)):null));
});
thi.ng.geom.viz.core.svg_plot2d_cartesian = (function thi$ng$geom$viz$core$svg_plot2d_cartesian(p__32864){
var map__32866 = p__32864;
var map__32866__$1 = ((cljs.core.seq_QMARK_.call(null,map__32866))?cljs.core.apply.call(null,cljs.core.hash_map,map__32866):map__32866);
var opts = map__32866__$1;
var x_axis = cljs.core.get.call(null,map__32866__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32866__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var grid = cljs.core.get.call(null,map__32866__$1,new cljs.core.Keyword(null,"grid","grid",402978600));
var data = cljs.core.get.call(null,map__32866__$1,new cljs.core.Keyword(null,"data","data",-232669377));
var opts__$1 = cljs.core.assoc.call(null,opts,new cljs.core.Keyword(null,"project","project",1124394579),thi.ng.geom.core.vector.vec2);
return thi.ng.geom.svg.core.group.call(null,cljs.core.PersistentArrayMap.EMPTY,(cljs.core.truth_(grid)?thi.ng.geom.viz.core.svg_axis_grid2d_cartesian.call(null,x_axis,y_axis,grid):null),cljs.core.map.call(null,((function (opts__$1,map__32866,map__32866__$1,opts,x_axis,y_axis,grid,data){
return (function (spec){
return new cljs.core.Keyword(null,"layout","layout",-2120940921).cljs$core$IFn$_invoke$arity$1(spec).call(null,opts__$1,spec);
});})(opts__$1,map__32866,map__32866__$1,opts,x_axis,y_axis,grid,data))
,data),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(x_axis))?thi.ng.geom.viz.core.svg_x_axis_cartesian.call(null,x_axis):null),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(y_axis))?thi.ng.geom.viz.core.svg_y_axis_cartesian.call(null,y_axis):null));
});
thi.ng.geom.viz.core.svg_x_axis_polar = (function thi$ng$geom$viz$core$svg_x_axis_polar(p__32870){
var map__32874 = p__32870;
var map__32874__$1 = ((cljs.core.seq_QMARK_.call(null,map__32874))?cljs.core.apply.call(null,cljs.core.hash_map,map__32874):map__32874);
var spec = map__32874__$1;
var map__32875 = cljs.core.get.call(null,map__32874__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var map__32875__$1 = ((cljs.core.seq_QMARK_.call(null,map__32875))?cljs.core.apply.call(null,cljs.core.hash_map,map__32875):map__32875);
var vec__32876 = cljs.core.get.call(null,map__32875__$1,new cljs.core.Keyword(null,"range","range",1639692286));
var r1 = cljs.core.nth.call(null,vec__32876,(0),null);
var r2 = cljs.core.nth.call(null,vec__32876,(1),null);
var scale = cljs.core.get.call(null,map__32875__$1,new cljs.core.Keyword(null,"scale","scale",-230427353));
var major_size = cljs.core.get.call(null,map__32875__$1,new cljs.core.Keyword(null,"major-size","major-size",-698672375));
var minor_size = cljs.core.get.call(null,map__32875__$1,new cljs.core.Keyword(null,"minor-size","minor-size",-1586355109));
var label_dist = cljs.core.get.call(null,map__32875__$1,new cljs.core.Keyword(null,"label-dist","label-dist",-538260526));
var pos = cljs.core.get.call(null,map__32875__$1,new cljs.core.Keyword(null,"pos","pos",-864607220));
var format = cljs.core.get.call(null,map__32875__$1,new cljs.core.Keyword(null,"format","format",-1306924766),thi.ng.geom.viz.core.value_formatter.call(null,(2)));
var project = cljs.core.get.call(null,map__32874__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var o = cljs.core.get.call(null,map__32874__$1,new cljs.core.Keyword(null,"origin","origin",1037372088));
return thi.ng.geom.viz.core.svg_axis_STAR_.call(null,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253).cljs$core$IFn$_invoke$arity$1(spec),(cljs.core.truth_(new cljs.core.Keyword(null,"circle","circle",1903212362).cljs$core$IFn$_invoke$arity$1(spec))?thi.ng.geom.svg.core.circle.call(null,o,pos,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"fill","fill",883462889),"none"], null)):thi.ng.geom.svg.core.arc.call(null,o,pos,r1,r2,(thi.ng.math.core.abs_diff.call(null,r1,r2) > thi.ng.math.core.PI),true,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"fill","fill",883462889),"none"], null))),((function (map__32874,map__32874__$1,spec,map__32875,map__32875__$1,vec__32876,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project,o){
return (function (p1__32867_SHARP_){
var x = scale.call(null,p1__32867_SHARP_);
return thi.ng.geom.svg.core.line.call(null,project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,pos], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,(pos + major_size)], null)));
});})(map__32874,map__32874__$1,spec,map__32875,map__32875__$1,vec__32876,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project,o))
,((function (map__32874,map__32874__$1,spec,map__32875,map__32875__$1,vec__32876,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project,o){
return (function (p1__32868_SHARP_){
var x = scale.call(null,p1__32868_SHARP_);
return thi.ng.geom.svg.core.line.call(null,project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,pos], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,(pos + minor_size)], null)));
});})(map__32874,map__32874__$1,spec,map__32875,map__32875__$1,vec__32876,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project,o))
,((function (map__32874,map__32874__$1,spec,map__32875,map__32875__$1,vec__32876,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project,o){
return (function (p1__32869_SHARP_){
var x = scale.call(null,p1__32869_SHARP_);
return thi.ng.geom.svg.core.text.call(null,project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,(pos + label_dist)], null)),format.call(null,p1__32869_SHARP_),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"none"], null));
});})(map__32874,map__32874__$1,spec,map__32875,map__32875__$1,vec__32876,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project,o))
);
});
thi.ng.geom.viz.core.svg_y_axis_polar = (function thi$ng$geom$viz$core$svg_y_axis_polar(p__32880){
var map__32884 = p__32880;
var map__32884__$1 = ((cljs.core.seq_QMARK_.call(null,map__32884))?cljs.core.apply.call(null,cljs.core.hash_map,map__32884):map__32884);
var spec = map__32884__$1;
var map__32885 = cljs.core.get.call(null,map__32884__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var map__32885__$1 = ((cljs.core.seq_QMARK_.call(null,map__32885))?cljs.core.apply.call(null,cljs.core.hash_map,map__32885):map__32885);
var vec__32886 = cljs.core.get.call(null,map__32885__$1,new cljs.core.Keyword(null,"range","range",1639692286));
var r1 = cljs.core.nth.call(null,vec__32886,(0),null);
var r2 = cljs.core.nth.call(null,vec__32886,(1),null);
var scale = cljs.core.get.call(null,map__32885__$1,new cljs.core.Keyword(null,"scale","scale",-230427353));
var major_size = cljs.core.get.call(null,map__32885__$1,new cljs.core.Keyword(null,"major-size","major-size",-698672375));
var minor_size = cljs.core.get.call(null,map__32885__$1,new cljs.core.Keyword(null,"minor-size","minor-size",-1586355109));
var label_dist = cljs.core.get.call(null,map__32885__$1,new cljs.core.Keyword(null,"label-dist","label-dist",-538260526));
var pos = cljs.core.get.call(null,map__32885__$1,new cljs.core.Keyword(null,"pos","pos",-864607220));
var format = cljs.core.get.call(null,map__32885__$1,new cljs.core.Keyword(null,"format","format",-1306924766),thi.ng.geom.viz.core.value_formatter.call(null,(2)));
var project = cljs.core.get.call(null,map__32884__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var a = project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,r1], null));
var b = project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,r2], null));
var nl = thi.ng.geom.core.normalize.call(null,thi.ng.geom.core.normal.call(null,thi.ng.geom.core._.call(null,a,b)),label_dist);
var n1 = thi.ng.geom.core.normalize.call(null,nl,major_size);
var n2 = thi.ng.geom.core.normalize.call(null,nl,minor_size);
return thi.ng.geom.viz.core.svg_axis_STAR_.call(null,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434).cljs$core$IFn$_invoke$arity$1(spec),thi.ng.geom.svg.core.line.call(null,a,b),((function (a,b,nl,n1,n2,map__32884,map__32884__$1,spec,map__32885,map__32885__$1,vec__32886,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project){
return (function (p1__32877_SHARP_){
var y = scale.call(null,p1__32877_SHARP_);
var p = project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,y], null));
return thi.ng.geom.svg.core.line.call(null,p,thi.ng.geom.core._PLUS_.call(null,p,n1));
});})(a,b,nl,n1,n2,map__32884,map__32884__$1,spec,map__32885,map__32885__$1,vec__32886,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project))
,((function (a,b,nl,n1,n2,map__32884,map__32884__$1,spec,map__32885,map__32885__$1,vec__32886,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project){
return (function (p1__32878_SHARP_){
var y = scale.call(null,p1__32878_SHARP_);
var p = project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,y], null));
return thi.ng.geom.svg.core.line.call(null,p,thi.ng.geom.core._PLUS_.call(null,p,n2));
});})(a,b,nl,n1,n2,map__32884,map__32884__$1,spec,map__32885,map__32885__$1,vec__32886,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project))
,((function (a,b,nl,n1,n2,map__32884,map__32884__$1,spec,map__32885,map__32885__$1,vec__32886,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project){
return (function (p1__32879_SHARP_){
var y = scale.call(null,p1__32879_SHARP_);
var p = project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [pos,y], null));
return thi.ng.geom.svg.core.text.call(null,thi.ng.geom.core._PLUS_.call(null,p,nl),format.call(null,p1__32879_SHARP_),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"none"], null));
});})(a,b,nl,n1,n2,map__32884,map__32884__$1,spec,map__32885,map__32885__$1,vec__32886,r1,r2,scale,major_size,minor_size,label_dist,pos,format,project))
);
});
thi.ng.geom.viz.core.svg_axis_grid2d_polar = (function thi$ng$geom$viz$core$svg_axis_grid2d_polar(p__32889){
var map__32894 = p__32889;
var map__32894__$1 = ((cljs.core.seq_QMARK_.call(null,map__32894))?cljs.core.apply.call(null,cljs.core.hash_map,map__32894):map__32894);
var map__32895 = cljs.core.get.call(null,map__32894__$1,new cljs.core.Keyword(null,"grid","grid",402978600));
var map__32895__$1 = ((cljs.core.seq_QMARK_.call(null,map__32895))?cljs.core.apply.call(null,cljs.core.hash_map,map__32895):map__32895);
var attribs = cljs.core.get.call(null,map__32895__$1,new cljs.core.Keyword(null,"attribs","attribs",-137878093));
var minor_x = cljs.core.get.call(null,map__32895__$1,new cljs.core.Keyword(null,"minor-x","minor-x",-230860299));
var minor_y = cljs.core.get.call(null,map__32895__$1,new cljs.core.Keyword(null,"minor-y","minor-y",388125550));
var x_axis = cljs.core.get.call(null,map__32894__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32894__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var origin = cljs.core.get.call(null,map__32894__$1,new cljs.core.Keyword(null,"origin","origin",1037372088));
var circle = cljs.core.get.call(null,map__32894__$1,new cljs.core.Keyword(null,"circle","circle",1903212362));
var project = cljs.core.get.call(null,map__32894__$1,new cljs.core.Keyword(null,"project","project",1124394579));
var vec__32896 = new cljs.core.Keyword(null,"range","range",1639692286).cljs$core$IFn$_invoke$arity$1(x_axis);
var x1 = cljs.core.nth.call(null,vec__32896,(0),null);
var x2 = cljs.core.nth.call(null,vec__32896,(1),null);
var vec__32897 = new cljs.core.Keyword(null,"range","range",1639692286).cljs$core$IFn$_invoke$arity$1(y_axis);
var y1 = cljs.core.nth.call(null,vec__32897,(0),null);
var y2 = cljs.core.nth.call(null,vec__32897,(1),null);
var scale_x = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(x_axis);
var scale_y = new cljs.core.Keyword(null,"scale","scale",-230427353).cljs$core$IFn$_invoke$arity$1(y_axis);
var great_QMARK_ = (thi.ng.math.core.abs_diff.call(null,x1,x2) > thi.ng.math.core.PI);
return thi.ng.geom.svg.core.group.call(null,cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"#ccc",new cljs.core.Keyword(null,"stroke-dasharray","stroke-dasharray",-942933855),"1 1"], null),attribs),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(x_axis))?cljs.core.map.call(null,((function (vec__32896,x1,x2,vec__32897,y1,y2,scale_x,scale_y,great_QMARK_,map__32894,map__32894__$1,map__32895,map__32895__$1,attribs,minor_x,minor_y,x_axis,y_axis,origin,circle,project){
return (function (p1__32887_SHARP_){
var x = scale_x.call(null,p1__32887_SHARP_);
return thi.ng.geom.svg.core.line.call(null,project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y1], null)),project.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y2], null)));
});})(vec__32896,x1,x2,vec__32897,y1,y2,scale_x,scale_y,great_QMARK_,map__32894,map__32894__$1,map__32895,map__32895__$1,attribs,minor_x,minor_y,x_axis,y_axis,origin,circle,project))
,thi.ng.geom.viz.core.select_ticks.call(null,x_axis,minor_x)):null),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(y_axis))?cljs.core.map.call(null,((function (vec__32896,x1,x2,vec__32897,y1,y2,scale_x,scale_y,great_QMARK_,map__32894,map__32894__$1,map__32895,map__32895__$1,attribs,minor_x,minor_y,x_axis,y_axis,origin,circle,project){
return (function (p1__32888_SHARP_){
var y = scale_y.call(null,p1__32888_SHARP_);
if(cljs.core.truth_(circle)){
return thi.ng.geom.svg.core.circle.call(null,origin,y,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"fill","fill",883462889),"none"], null));
} else {
return thi.ng.geom.svg.core.arc.call(null,origin,y,x1,x2,great_QMARK_,true,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"fill","fill",883462889),"none"], null));
}
});})(vec__32896,x1,x2,vec__32897,y1,y2,scale_x,scale_y,great_QMARK_,map__32894,map__32894__$1,map__32895,map__32895__$1,attribs,minor_x,minor_y,x_axis,y_axis,origin,circle,project))
,thi.ng.geom.viz.core.select_ticks.call(null,y_axis,minor_y)):null));
});
thi.ng.geom.viz.core.svg_plot2d_polar = (function thi$ng$geom$viz$core$svg_plot2d_polar(p__32898){
var map__32900 = p__32898;
var map__32900__$1 = ((cljs.core.seq_QMARK_.call(null,map__32900))?cljs.core.apply.call(null,cljs.core.hash_map,map__32900):map__32900);
var opts = map__32900__$1;
var x_axis = cljs.core.get.call(null,map__32900__$1,new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253));
var y_axis = cljs.core.get.call(null,map__32900__$1,new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434));
var grid = cljs.core.get.call(null,map__32900__$1,new cljs.core.Keyword(null,"grid","grid",402978600));
var data = cljs.core.get.call(null,map__32900__$1,new cljs.core.Keyword(null,"data","data",-232669377));
var origin = cljs.core.get.call(null,map__32900__$1,new cljs.core.Keyword(null,"origin","origin",1037372088));
var opts__$1 = cljs.core.assoc.call(null,opts,new cljs.core.Keyword(null,"project","project",1124394579),thi.ng.geom.viz.core.polar_projection.call(null,origin));
return thi.ng.geom.svg.core.group.call(null,cljs.core.PersistentArrayMap.EMPTY,(cljs.core.truth_(grid)?thi.ng.geom.viz.core.svg_axis_grid2d_polar.call(null,opts__$1):null),cljs.core.map.call(null,((function (opts__$1,map__32900,map__32900__$1,opts,x_axis,y_axis,grid,data,origin){
return (function (spec){
return new cljs.core.Keyword(null,"layout","layout",-2120940921).cljs$core$IFn$_invoke$arity$1(spec).call(null,opts__$1,spec);
});})(opts__$1,map__32900,map__32900__$1,opts,x_axis,y_axis,grid,data,origin))
,data),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(x_axis))?thi.ng.geom.viz.core.svg_x_axis_polar.call(null,opts__$1):null),(cljs.core.truth_(new cljs.core.Keyword(null,"visible","visible",-1024216805).cljs$core$IFn$_invoke$arity$1(y_axis))?thi.ng.geom.viz.core.svg_y_axis_polar.call(null,opts__$1):null));
});

//# sourceMappingURL=core.js.map?rel=1439206040333
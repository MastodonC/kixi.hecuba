// Compiled by ClojureScript 0.0-3297 {}
goog.provide('geom_om.heatmap');
goog.require('cljs.core');
goog.require('thi.ng.geom.core.utils');
goog.require('thi.ng.geom.viz.core');
goog.require('goog.string');
goog.require('thi.ng.color.gradients');
goog.require('om.dom');
goog.require('hiccups.runtime');
goog.require('thi.ng.geom.core');
goog.require('thi.ng.geom.core.vector');
goog.require('cljs_http.client');
goog.require('thi.ng.math.simplexnoise');
goog.require('thi.ng.geom.svg.core');
goog.require('bardo.interpolate');
goog.require('thi.ng.math.core');
goog.require('cljs.core.async');
goog.require('goog.string.format');
goog.require('om.core');
goog.require('cljs.reader');
geom_om.heatmap.x_readings = (14);
geom_om.heatmap.y_readings = (48);
geom_om.heatmap.chart_width = cljs.core.atom.call(null,(800));
geom_om.heatmap.chart_height = cljs.core.atom.call(null,(600));
geom_om.heatmap.chart_data_chan = cljs.core.atom.call(null,null);
geom_om.heatmap.chart_fill_oor_cells_QMARK_ = cljs.core.atom.call(null,true);
geom_om.heatmap.default_gradations = (20);
/**
 * http://colorbrewer2.org/?type=diverging&scheme=RdYlBu&n=10
 */
geom_om.heatmap.colour_scheme = new cljs.core.PersistentVector(null, 10, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(49),(54),(149)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(69),(117),(180)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(116),(173),(209)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(171),(217),(233)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(224),(243),(248)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(254),(224),(144)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(253),(174),(97)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(244),(109),(67)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(215),(48),(39)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(165),(0),(38)], null)], null);
/**
 * Generates n colours based on the gradations and color range specified - EXPENSIVE
 */
geom_om.heatmap.generate_palette = (function geom_om$heatmap$generate_palette(grads,colours){
var times = cljs.core.map.call(null,(function (p1__30521_SHARP_){
return ((p1__30521_SHARP_ + (1)) / grads);
}),cljs.core.range.call(null,grads));
var rgb_funcs = new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"red","red",-969428204),cljs.core.first,new cljs.core.Keyword(null,"green","green",-945526839),cljs.core.second,new cljs.core.Keyword(null,"blue","blue",-622100620),cljs.core.last], null);
return cljs.core.mapv.call(null,((function (times,rgb_funcs){
return (function (t){
return cljs.core.mapv.call(null,((function (times,rgb_funcs){
return (function (p__30525){
var vec__30526 = p__30525;
var k = cljs.core.nth.call(null,vec__30526,(0),null);
var v = cljs.core.nth.call(null,vec__30526,(1),null);
return bardo.interpolate.pipeline.call(null,cljs.core.map.call(null,((function (vec__30526,k,v,times,rgb_funcs){
return (function (p1__30522_SHARP_){
return (v.call(null,p1__30522_SHARP_) / (255));
});})(vec__30526,k,v,times,rgb_funcs))
,colours)).call(null,t);
});})(times,rgb_funcs))
,rgb_funcs);
});})(times,rgb_funcs))
,times);
});
geom_om.heatmap.heatmap_spec = (function geom_om$heatmap$heatmap_spec(id,heatmap_data,size_x,size_y,lcb,ucb,grads){
var matrix = thi.ng.geom.viz.core.matrix_2d.call(null,size_x,size_y,heatmap_data);
return new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"matrix","matrix",803137200),matrix,new cljs.core.Keyword(null,"value-domain","value-domain",1224230851),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [lcb,ucb], null),new cljs.core.Keyword(null,"palette","palette",-456203511),geom_om.heatmap.generate_palette.call(null,grads,geom_om.heatmap.colour_scheme),new cljs.core.Keyword(null,"palette-scale","palette-scale",2003276610),thi.ng.geom.viz.core.linear_scale,new cljs.core.Keyword(null,"layout","layout",-2120940921),thi.ng.geom.viz.core.svg_heatmap], null);
});
geom_om.heatmap.int_to_dow = (function geom_om$heatmap$int_to_dow(num){
return cljs.core.nth.call(null,new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, ["S","M","T","W","T","F","S"], null),cljs.core.mod.call(null,num,(7)));
});
geom_om.heatmap.int_to_tod = (function geom_om$heatmap$int_to_tod(num){
var hrs = cljs.core.mod.call(null,Math.floor((num / (2))),(24));
var mins = (((cljs.core.mod.call(null,num,(2)) === (0)))?(0):(30));
return goog.string.format("%02d:%02d",hrs,mins);
});
geom_om.heatmap.set_new_heatmap_data_BANG_ = (function geom_om$heatmap$set_new_heatmap_data_BANG_(cursor,data,lcb,ucb,gradations){
var lcb__$1 = (((lcb == null))?Math.floor(cljs.core.apply.call(null,cljs.core.min,data)):lcb);
var ucb__$1 = (((ucb == null))?Math.ceil(cljs.core.apply.call(null,cljs.core.max,data)):ucb);
var gradations__$1 = (((gradations == null))?geom_om.heatmap.default_gradations:gradations);
var adjusted_data = (cljs.core.truth_(cljs.core.deref.call(null,geom_om.heatmap.chart_fill_oor_cells_QMARK_))?cljs.core.map.call(null,((function (lcb__$1,ucb__$1,gradations__$1){
return (function (p1__30527_SHARP_){
return thi.ng.math.core.clamp.call(null,p1__30527_SHARP_,lcb__$1,ucb__$1);
});})(lcb__$1,ucb__$1,gradations__$1))
,data):data);
om.core.update_BANG_.call(null,cursor,new cljs.core.Keyword(null,"data","data",-232669377),data);

om.core.update_BANG_.call(null,cursor,new cljs.core.Keyword(null,"element","element",1974019749),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253),thi.ng.geom.viz.core.linear_axis.call(null,new cljs.core.PersistentArrayMap(null, 8, [new cljs.core.Keyword(null,"domain","domain",1847214937),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),geom_om.heatmap.x_readings], null),new cljs.core.Keyword(null,"range","range",1639692286),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(55),(cljs.core.deref.call(null,geom_om.heatmap.chart_width) + (5))], null),new cljs.core.Keyword(null,"major","major",-27376078),(1),new cljs.core.Keyword(null,"pos","pos",-864607220),(30),new cljs.core.Keyword(null,"label-dist","label-dist",-538260526),(-10),new cljs.core.Keyword(null,"major-size","major-size",-698672375),(-5),new cljs.core.Keyword(null,"format","format",-1306924766),((function (lcb__$1,ucb__$1,gradations__$1,adjusted_data){
return (function (p1__30528_SHARP_){
return geom_om.heatmap.int_to_dow.call(null,(p1__30528_SHARP_ | (0)));
});})(lcb__$1,ucb__$1,gradations__$1,adjusted_data))
,new cljs.core.Keyword(null,"label","label",1718410804),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"text-anchor","text-anchor",585613696),"right"], null)], null)),new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434),thi.ng.geom.viz.core.linear_axis.call(null,new cljs.core.PersistentArrayMap(null, 7, [new cljs.core.Keyword(null,"domain","domain",1847214937),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),geom_om.heatmap.y_readings], null),new cljs.core.Keyword(null,"range","range",1639692286),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(cljs.core.deref.call(null,geom_om.heatmap.chart_height) - (10)),(35)], null),new cljs.core.Keyword(null,"major","major",-27376078),(1),new cljs.core.Keyword(null,"pos","pos",-864607220),(50),new cljs.core.Keyword(null,"label-dist","label-dist",-538260526),(15),new cljs.core.Keyword(null,"format","format",-1306924766),((function (lcb__$1,ucb__$1,gradations__$1,adjusted_data){
return (function (p1__30529_SHARP_){
return geom_om.heatmap.int_to_tod.call(null,(geom_om.heatmap.y_readings - p1__30529_SHARP_));
});})(lcb__$1,ucb__$1,gradations__$1,adjusted_data))
,new cljs.core.Keyword(null,"label","label",1718410804),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"text-anchor","text-anchor",585613696),"end"], null)], null)),new cljs.core.Keyword(null,"data","data",-232669377),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.merge.call(null,geom_om.heatmap.heatmap_spec.call(null,new cljs.core.Keyword(null,"yellow-magenta-cyan","yellow-magenta-cyan",-256131530),adjusted_data,geom_om.heatmap.x_readings,geom_om.heatmap.y_readings,lcb__$1,ucb__$1,gradations__$1),null)], null)], null));

return om.core.update_BANG_.call(null,cursor,new cljs.core.Keyword(null,"element-legend","element-legend",1189215408),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253),thi.ng.geom.viz.core.linear_axis.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"domain","domain",1847214937),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(20)], null),new cljs.core.Keyword(null,"range","range",1639692286),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(20),(400)], null),new cljs.core.Keyword(null,"visible","visible",-1024216805),false], null)),new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434),thi.ng.geom.viz.core.linear_axis.call(null,new cljs.core.PersistentArrayMap(null, 8, [new cljs.core.Keyword(null,"domain","domain",1847214937),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),gradations__$1], null),new cljs.core.Keyword(null,"range","range",1639692286),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(cljs.core.deref.call(null,geom_om.heatmap.chart_height) - (10)),(35)], null),new cljs.core.Keyword(null,"major","major",-27376078),(1),new cljs.core.Keyword(null,"major-size","major-size",-698672375),(0),new cljs.core.Keyword(null,"pos","pos",-864607220),(10),new cljs.core.Keyword(null,"label-dist","label-dist",-538260526),(-35),new cljs.core.Keyword(null,"label","label",1718410804),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"text-anchor","text-anchor",585613696),"start"], null),new cljs.core.Keyword(null,"format","format",-1306924766),((function (lcb__$1,ucb__$1,gradations__$1,adjusted_data){
return (function (p1__30530_SHARP_){
if(cljs.core._EQ_.call(null,p1__30530_SHARP_,(0))){
return lcb__$1;
} else {
if(cljs.core._EQ_.call(null,p1__30530_SHARP_,gradations__$1)){
return ucb__$1;
} else {
return null;
}
}
});})(lcb__$1,ucb__$1,gradations__$1,adjusted_data))
], null)),new cljs.core.Keyword(null,"data","data",-232669377),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.merge.call(null,geom_om.heatmap.heatmap_spec.call(null,new cljs.core.Keyword(null,"yellow-magenta-cyan","yellow-magenta-cyan",-256131530),cljs.core.vec.call(null,(function (){var iter__16823__auto__ = ((function (lcb__$1,ucb__$1,gradations__$1,adjusted_data){
return (function geom_om$heatmap$set_new_heatmap_data_BANG__$_iter__30536(s__30537){
return (new cljs.core.LazySeq(null,((function (lcb__$1,ucb__$1,gradations__$1,adjusted_data){
return (function (){
var s__30537__$1 = s__30537;
while(true){
var temp__4425__auto__ = cljs.core.seq.call(null,s__30537__$1);
if(temp__4425__auto__){
var s__30537__$2 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,s__30537__$2)){
var c__16821__auto__ = cljs.core.chunk_first.call(null,s__30537__$2);
var size__16822__auto__ = cljs.core.count.call(null,c__16821__auto__);
var b__30539 = cljs.core.chunk_buffer.call(null,size__16822__auto__);
if((function (){var i__30538 = (0);
while(true){
if((i__30538 < size__16822__auto__)){
var x = cljs.core._nth.call(null,c__16821__auto__,i__30538);
cljs.core.chunk_append.call(null,b__30539,thi.ng.math.core.mix.call(null,lcb__$1,ucb__$1,x));

var G__30540 = (i__30538 + (1));
i__30538 = G__30540;
continue;
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__30539),geom_om$heatmap$set_new_heatmap_data_BANG__$_iter__30536.call(null,cljs.core.chunk_rest.call(null,s__30537__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__30539),null);
}
} else {
var x = cljs.core.first.call(null,s__30537__$2);
return cljs.core.cons.call(null,thi.ng.math.core.mix.call(null,lcb__$1,ucb__$1,x),geom_om$heatmap$set_new_heatmap_data_BANG__$_iter__30536.call(null,cljs.core.rest.call(null,s__30537__$2)));
}
} else {
return null;
}
break;
}
});})(lcb__$1,ucb__$1,gradations__$1,adjusted_data))
,null,null));
});})(lcb__$1,ucb__$1,gradations__$1,adjusted_data))
;
return iter__16823__auto__.call(null,cljs.core.map.call(null,((function (iter__16823__auto__,lcb__$1,ucb__$1,gradations__$1,adjusted_data){
return (function (p1__30531_SHARP_){
return (p1__30531_SHARP_ / gradations__$1);
});})(iter__16823__auto__,lcb__$1,ucb__$1,gradations__$1,adjusted_data))
,cljs.core.range.call(null,(0),gradations__$1)));
})()),(1),gradations__$1,lcb__$1,ucb__$1,gradations__$1),null)], null)], null));
});
geom_om.heatmap.data_loop = (function geom_om$heatmap$data_loop(cursor,input_chan){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_30634){
var state_val_30635 = (state_30634[(1)]);
if((state_val_30635 === (7))){
var inst_30632 = (state_30634[(2)]);
var state_30634__$1 = state_30634;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_30634__$1,inst_30632);
} else {
if((state_val_30635 === (1))){
var state_30634__$1 = state_30634;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_30634__$1,(2),input_chan);
} else {
if((state_val_30635 === (4))){
var inst_30603 = (state_30634[(7)]);
var state_30634__$1 = state_30634;
var statearr_30636_30657 = state_30634__$1;
(statearr_30636_30657[(2)] = inst_30603);

(statearr_30636_30657[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30635 === (6))){
var inst_30614 = (state_30634[(8)]);
var inst_30617 = cljs.core.seq_QMARK_.call(null,inst_30614);
var state_30634__$1 = state_30634;
if(inst_30617){
var statearr_30637_30658 = state_30634__$1;
(statearr_30637_30658[(1)] = (8));

} else {
var statearr_30638_30659 = state_30634__$1;
(statearr_30638_30659[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30635 === (3))){
var inst_30603 = (state_30634[(7)]);
var inst_30606 = cljs.core.apply.call(null,cljs.core.hash_map,inst_30603);
var state_30634__$1 = state_30634;
var statearr_30639_30660 = state_30634__$1;
(statearr_30639_30660[(2)] = inst_30606);

(statearr_30639_30660[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30635 === (2))){
var inst_30603 = (state_30634[(7)]);
var inst_30603__$1 = (state_30634[(2)]);
var inst_30604 = cljs.core.seq_QMARK_.call(null,inst_30603__$1);
var state_30634__$1 = (function (){var statearr_30640 = state_30634;
(statearr_30640[(7)] = inst_30603__$1);

return statearr_30640;
})();
if(inst_30604){
var statearr_30641_30661 = state_30634__$1;
(statearr_30641_30661[(1)] = (3));

} else {
var statearr_30642_30662 = state_30634__$1;
(statearr_30642_30662[(1)] = (4));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30635 === (11))){
var inst_30629 = (state_30634[(2)]);
var inst_30614 = inst_30629;
var state_30634__$1 = (function (){var statearr_30643 = state_30634;
(statearr_30643[(8)] = inst_30614);

return statearr_30643;
})();
var statearr_30644_30663 = state_30634__$1;
(statearr_30644_30663[(2)] = null);

(statearr_30644_30663[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30635 === (9))){
var inst_30614 = (state_30634[(8)]);
var state_30634__$1 = state_30634;
var statearr_30645_30664 = state_30634__$1;
(statearr_30645_30664[(2)] = inst_30614);

(statearr_30645_30664[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30635 === (5))){
var inst_30603 = (state_30634[(7)]);
var inst_30609 = (state_30634[(2)]);
var inst_30610 = cljs.core.get.call(null,inst_30609,new cljs.core.Keyword(null,"data","data",-232669377));
var inst_30611 = cljs.core.get.call(null,inst_30609,new cljs.core.Keyword(null,"lcb","lcb",1646475679));
var inst_30612 = cljs.core.get.call(null,inst_30609,new cljs.core.Keyword(null,"ucb","ucb",19334455));
var inst_30613 = cljs.core.get.call(null,inst_30609,new cljs.core.Keyword(null,"grads","grads",-33995815));
var inst_30614 = inst_30603;
var state_30634__$1 = (function (){var statearr_30646 = state_30634;
(statearr_30646[(8)] = inst_30614);

(statearr_30646[(9)] = inst_30612);

(statearr_30646[(10)] = inst_30613);

(statearr_30646[(11)] = inst_30610);

(statearr_30646[(12)] = inst_30611);

return statearr_30646;
})();
var statearr_30647_30665 = state_30634__$1;
(statearr_30647_30665[(2)] = null);

(statearr_30647_30665[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30635 === (10))){
var inst_30622 = (state_30634[(2)]);
var inst_30623 = cljs.core.get.call(null,inst_30622,new cljs.core.Keyword(null,"data","data",-232669377));
var inst_30624 = cljs.core.get.call(null,inst_30622,new cljs.core.Keyword(null,"lcb","lcb",1646475679));
var inst_30625 = cljs.core.get.call(null,inst_30622,new cljs.core.Keyword(null,"ucb","ucb",19334455));
var inst_30626 = cljs.core.get.call(null,inst_30622,new cljs.core.Keyword(null,"grads","grads",-33995815));
var inst_30627 = geom_om.heatmap.set_new_heatmap_data_BANG_.call(null,cursor,inst_30623,inst_30624,inst_30625,inst_30626);
var state_30634__$1 = (function (){var statearr_30648 = state_30634;
(statearr_30648[(13)] = inst_30627);

return statearr_30648;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_30634__$1,(11),input_chan);
} else {
if((state_val_30635 === (8))){
var inst_30614 = (state_30634[(8)]);
var inst_30619 = cljs.core.apply.call(null,cljs.core.hash_map,inst_30614);
var state_30634__$1 = state_30634;
var statearr_30649_30666 = state_30634__$1;
(statearr_30649_30666[(2)] = inst_30619);

(statearr_30649_30666[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var geom_om$heatmap$data_loop_$_state_machine__23572__auto__ = null;
var geom_om$heatmap$data_loop_$_state_machine__23572__auto____0 = (function (){
var statearr_30653 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_30653[(0)] = geom_om$heatmap$data_loop_$_state_machine__23572__auto__);

(statearr_30653[(1)] = (1));

return statearr_30653;
});
var geom_om$heatmap$data_loop_$_state_machine__23572__auto____1 = (function (state_30634){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_30634);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e30654){if((e30654 instanceof Object)){
var ex__23575__auto__ = e30654;
var statearr_30655_30667 = state_30634;
(statearr_30655_30667[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_30634);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e30654;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__30668 = state_30634;
state_30634 = G__30668;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
geom_om$heatmap$data_loop_$_state_machine__23572__auto__ = function(state_30634){
switch(arguments.length){
case 0:
return geom_om$heatmap$data_loop_$_state_machine__23572__auto____0.call(this);
case 1:
return geom_om$heatmap$data_loop_$_state_machine__23572__auto____1.call(this,state_30634);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
geom_om$heatmap$data_loop_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = geom_om$heatmap$data_loop_$_state_machine__23572__auto____0;
geom_om$heatmap$data_loop_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = geom_om$heatmap$data_loop_$_state_machine__23572__auto____1;
return geom_om$heatmap$data_loop_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_30656 = f__23634__auto__.call(null);
(statearr_30656[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_30656;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});
geom_om.heatmap.chart = (function geom_om$heatmap$chart(p__30669){
var map__30674 = p__30669;
var map__30674__$1 = ((cljs.core.seq_QMARK_.call(null,map__30674))?cljs.core.apply.call(null,cljs.core.hash_map,map__30674):map__30674);
var width = cljs.core.get.call(null,map__30674__$1,new cljs.core.Keyword(null,"width","width",-384071477),(800));
var height = cljs.core.get.call(null,map__30674__$1,new cljs.core.Keyword(null,"height","height",1025178622),(600));
var data_chan = cljs.core.get.call(null,map__30674__$1,new cljs.core.Keyword(null,"data-chan","data-chan",-55900851));
var fill_out_of_range_cells_QMARK_ = cljs.core.get.call(null,map__30674__$1,new cljs.core.Keyword(null,"fill-out-of-range-cells?","fill-out-of-range-cells?",775970238));
if((data_chan == null)){
throw (new Error("Heatmap requires a data channel!"));
} else {
cljs.core.reset_BANG_.call(null,geom_om.heatmap.chart_data_chan,data_chan);
}

cljs.core.reset_BANG_.call(null,geom_om.heatmap.chart_width,width);

cljs.core.reset_BANG_.call(null,geom_om.heatmap.chart_height,height);

cljs.core.reset_BANG_.call(null,geom_om.heatmap.chart_fill_oor_cells_QMARK_,fill_out_of_range_cells_QMARK_);

return ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function (cursor,owner){
if(typeof geom_om.heatmap.t30675 !== 'undefined'){
} else {

/**
* @constructor
*/
geom_om.heatmap.t30675 = (function (chart,p__30669,map__30674,width,height,data_chan,fill_out_of_range_cells_QMARK_,cursor,owner,meta30676){
this.chart = chart;
this.p__30669 = p__30669;
this.map__30674 = map__30674;
this.width = width;
this.height = height;
this.data_chan = data_chan;
this.fill_out_of_range_cells_QMARK_ = fill_out_of_range_cells_QMARK_;
this.cursor = cursor;
this.owner = owner;
this.meta30676 = meta30676;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
geom_om.heatmap.t30675.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function (_30677,meta30676__$1){
var self__ = this;
var _30677__$1 = this;
return (new geom_om.heatmap.t30675(self__.chart,self__.p__30669,self__.map__30674,self__.width,self__.height,self__.data_chan,self__.fill_out_of_range_cells_QMARK_,self__.cursor,self__.owner,meta30676__$1));
});})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
;

geom_om.heatmap.t30675.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function (_30677){
var self__ = this;
var _30677__$1 = this;
return self__.meta30676;
});})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
;

geom_om.heatmap.t30675.prototype.om$core$IWillMount$ = true;

geom_om.heatmap.t30675.prototype.om$core$IWillMount$will_mount$arity$1 = ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function (_){
var self__ = this;
var ___$1 = this;
return geom_om.heatmap.data_loop.call(null,self__.cursor,cljs.core.deref.call(null,geom_om.heatmap.chart_data_chan));
});})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
;

geom_om.heatmap.t30675.prototype.om$core$IRender$ = true;

geom_om.heatmap.t30675.prototype.om$core$IRender$render$arity$1 = ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function (_){
var self__ = this;
var ___$1 = this;
return React.DOM.div({"style": {"position": "relative", "overflow": "hidden", "whiteSpace": "nowrap"}},React.DOM.div({"style": {"display": "inline-block"}, "dangerouslySetInnerHTML": {"__html": [cljs.core.str(hiccups.runtime.render_html.call(null,thi.ng.geom.svg.core.svg.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"width","width",-384071477),cljs.core.deref.call(null,geom_om.heatmap.chart_width),new cljs.core.Keyword(null,"height","height",1025178622),cljs.core.deref.call(null,geom_om.heatmap.chart_height)], null),thi.ng.geom.viz.core.svg_plot2d_cartesian.call(null,new cljs.core.Keyword(null,"element","element",1974019749).cljs$core$IFn$_invoke$arity$1(self__.cursor)))))].join('')}}),React.DOM.div({"style": {"display": "inline-block"}, "dangerouslySetInnerHTML": {"__html": [cljs.core.str(hiccups.runtime.render_html.call(null,thi.ng.geom.svg.core.svg.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"width","width",-384071477),cljs.core.deref.call(null,geom_om.heatmap.chart_width),new cljs.core.Keyword(null,"height","height",1025178622),cljs.core.deref.call(null,geom_om.heatmap.chart_height)], null),thi.ng.geom.viz.core.svg_plot2d_cartesian.call(null,new cljs.core.Keyword(null,"element-legend","element-legend",1189215408).cljs$core$IFn$_invoke$arity$1(self__.cursor)))))].join('')}}));
});})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
;

geom_om.heatmap.t30675.getBasis = ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function (){
return new cljs.core.PersistentVector(null, 10, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"chart","chart",-1481210344,null),new cljs.core.Symbol(null,"p__30669","p__30669",-1181966973,null),new cljs.core.Symbol(null,"map__30674","map__30674",341619691,null),new cljs.core.Symbol(null,"width","width",1256460050,null),new cljs.core.Symbol(null,"height","height",-1629257147,null),new cljs.core.Symbol(null,"data-chan","data-chan",1584630676,null),new cljs.core.Symbol(null,"fill-out-of-range-cells?","fill-out-of-range-cells?",-1878465531,null),new cljs.core.Symbol(null,"cursor","cursor",-1642498285,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"meta30676","meta30676",-626945935,null)], null);
});})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
;

geom_om.heatmap.t30675.cljs$lang$type = true;

geom_om.heatmap.t30675.cljs$lang$ctorStr = "geom-om.heatmap/t30675";

geom_om.heatmap.t30675.cljs$lang$ctorPrWriter = ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"geom-om.heatmap/t30675");
});})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
;

geom_om.heatmap.__GT_t30675 = ((function (map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_){
return (function geom_om$heatmap$chart_$___GT_t30675(chart__$1,p__30669__$1,map__30674__$2,width__$1,height__$1,data_chan__$1,fill_out_of_range_cells_QMARK___$1,cursor__$1,owner__$1,meta30676){
return (new geom_om.heatmap.t30675(chart__$1,p__30669__$1,map__30674__$2,width__$1,height__$1,data_chan__$1,fill_out_of_range_cells_QMARK___$1,cursor__$1,owner__$1,meta30676));
});})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
;

}

return (new geom_om.heatmap.t30675(geom_om$heatmap$chart,p__30669,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_,cursor,owner,cljs.core.PersistentArrayMap.EMPTY));
});
;})(map__30674,map__30674__$1,width,height,data_chan,fill_out_of_range_cells_QMARK_))
});

//# sourceMappingURL=heatmap.js.map?rel=1439206034126
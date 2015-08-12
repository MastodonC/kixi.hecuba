// Compiled by ClojureScript 0.0-3297 {}
goog.provide('geom_om.xy');
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
goog.require('thi.ng.math.core');
goog.require('cljs.core.async');
goog.require('goog.string.format');
goog.require('om.core');
goog.require('cljs.reader');
geom_om.xy.chart_width = cljs.core.atom.call(null,(800));
geom_om.xy.chart_height = cljs.core.atom.call(null,(600));
geom_om.xy.chart_x_range = cljs.core.atom.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(200)], null));
geom_om.xy.chart_y_range = cljs.core.atom.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(200)], null));
geom_om.xy.chart_data_chan = cljs.core.atom.call(null,null);
geom_om.xy.set_new_xy_plot_data_BANG_ = (function geom_om$xy$set_new_xy_plot_data_BANG_(cursor,data){
return om.core.update_BANG_.call(null,cursor,new cljs.core.Keyword(null,"element","element",1974019749),new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"x-axis","x-axis",-1736373253),thi.ng.geom.viz.core.linear_axis.call(null,new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"domain","domain",1847214937),cljs.core.deref.call(null,geom_om.xy.chart_x_range),new cljs.core.Keyword(null,"range","range",1639692286),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(50),(cljs.core.deref.call(null,geom_om.xy.chart_width) - (10))], null),new cljs.core.Keyword(null,"pos","pos",-864607220),(cljs.core.deref.call(null,geom_om.xy.chart_height) - (20)),new cljs.core.Keyword(null,"major","major",-27376078),(20),new cljs.core.Keyword(null,"minor","minor",-608536071),(10)], null)),new cljs.core.Keyword(null,"y-axis","y-axis",-1055729434),thi.ng.geom.viz.core.linear_axis.call(null,new cljs.core.PersistentArrayMap(null, 7, [new cljs.core.Keyword(null,"domain","domain",1847214937),cljs.core.deref.call(null,geom_om.xy.chart_y_range),new cljs.core.Keyword(null,"range","range",1639692286),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(cljs.core.deref.call(null,geom_om.xy.chart_height) - (20)),(20)], null),new cljs.core.Keyword(null,"major","major",-27376078),(10),new cljs.core.Keyword(null,"minor","minor",-608536071),(5),new cljs.core.Keyword(null,"pos","pos",-864607220),(50),new cljs.core.Keyword(null,"label-dist","label-dist",-538260526),(15),new cljs.core.Keyword(null,"label","label",1718410804),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"text-anchor","text-anchor",585613696),"end"], null)], null)),new cljs.core.Keyword(null,"grid","grid",402978600),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"attribs","attribs",-137878093),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"stroke","stroke",1741823555),"#caa"], null),new cljs.core.Keyword(null,"minor-x","minor-x",-230860299),true,new cljs.core.Keyword(null,"minor-y","minor-y",388125550),true], null),new cljs.core.Keyword(null,"data","data",-232669377),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"values","values",372645556),new cljs.core.Keyword(null,"values","values",372645556).cljs$core$IFn$_invoke$arity$1(data),new cljs.core.Keyword(null,"attribs","attribs",-137878093),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"fill","fill",883462889),"#06f",new cljs.core.Keyword(null,"stroke","stroke",1741823555),"#06f"], null),new cljs.core.Keyword(null,"shape","shape",1190694006),thi.ng.geom.viz.core.svg_square.call(null,(2)),new cljs.core.Keyword(null,"layout","layout",-2120940921),thi.ng.geom.viz.core.svg_scatter_plot], null),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"values","values",372645556),new cljs.core.Keyword(null,"line","line",212345235).cljs$core$IFn$_invoke$arity$1(data),new cljs.core.Keyword(null,"attribs","attribs",-137878093),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"fill","fill",883462889),"none",new cljs.core.Keyword(null,"stroke","stroke",1741823555),"#f23"], null),new cljs.core.Keyword(null,"layout","layout",-2120940921),thi.ng.geom.viz.core.svg_line_plot], null)], null)], null));
});
geom_om.xy.data_loop = (function geom_om$xy$data_loop(cursor,input_chan){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_30716){
var state_val_30717 = (state_30716[(1)]);
if((state_val_30717 === (1))){
var state_30716__$1 = state_30716;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_30716__$1,(2),input_chan);
} else {
if((state_val_30717 === (2))){
var inst_30706 = (state_30716[(2)]);
var inst_30707 = inst_30706;
var state_30716__$1 = (function (){var statearr_30718 = state_30716;
(statearr_30718[(7)] = inst_30707);

return statearr_30718;
})();
var statearr_30719_30730 = state_30716__$1;
(statearr_30719_30730[(2)] = null);

(statearr_30719_30730[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_30717 === (3))){
var inst_30707 = (state_30716[(7)]);
var inst_30709 = geom_om.xy.set_new_xy_plot_data_BANG_.call(null,cursor,inst_30707);
var state_30716__$1 = (function (){var statearr_30720 = state_30716;
(statearr_30720[(8)] = inst_30709);

return statearr_30720;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_30716__$1,(5),input_chan);
} else {
if((state_val_30717 === (4))){
var inst_30714 = (state_30716[(2)]);
var state_30716__$1 = state_30716;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_30716__$1,inst_30714);
} else {
if((state_val_30717 === (5))){
var inst_30711 = (state_30716[(2)]);
var inst_30707 = inst_30711;
var state_30716__$1 = (function (){var statearr_30721 = state_30716;
(statearr_30721[(7)] = inst_30707);

return statearr_30721;
})();
var statearr_30722_30731 = state_30716__$1;
(statearr_30722_30731[(2)] = null);

(statearr_30722_30731[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var geom_om$xy$data_loop_$_state_machine__23572__auto__ = null;
var geom_om$xy$data_loop_$_state_machine__23572__auto____0 = (function (){
var statearr_30726 = [null,null,null,null,null,null,null,null,null];
(statearr_30726[(0)] = geom_om$xy$data_loop_$_state_machine__23572__auto__);

(statearr_30726[(1)] = (1));

return statearr_30726;
});
var geom_om$xy$data_loop_$_state_machine__23572__auto____1 = (function (state_30716){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_30716);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e30727){if((e30727 instanceof Object)){
var ex__23575__auto__ = e30727;
var statearr_30728_30732 = state_30716;
(statearr_30728_30732[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_30716);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e30727;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__30733 = state_30716;
state_30716 = G__30733;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
geom_om$xy$data_loop_$_state_machine__23572__auto__ = function(state_30716){
switch(arguments.length){
case 0:
return geom_om$xy$data_loop_$_state_machine__23572__auto____0.call(this);
case 1:
return geom_om$xy$data_loop_$_state_machine__23572__auto____1.call(this,state_30716);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
geom_om$xy$data_loop_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = geom_om$xy$data_loop_$_state_machine__23572__auto____0;
geom_om$xy$data_loop_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = geom_om$xy$data_loop_$_state_machine__23572__auto____1;
return geom_om$xy$data_loop_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_30729 = f__23634__auto__.call(null);
(statearr_30729[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_30729;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});
geom_om.xy.chart = (function geom_om$xy$chart(p__30734){
var map__30739 = p__30734;
var map__30739__$1 = ((cljs.core.seq_QMARK_.call(null,map__30739))?cljs.core.apply.call(null,cljs.core.hash_map,map__30739):map__30739);
var width = cljs.core.get.call(null,map__30739__$1,new cljs.core.Keyword(null,"width","width",-384071477),(800));
var height = cljs.core.get.call(null,map__30739__$1,new cljs.core.Keyword(null,"height","height",1025178622),(600));
var x_range = cljs.core.get.call(null,map__30739__$1,new cljs.core.Keyword(null,"x-range","x-range",1820649693),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(200)], null));
var y_range = cljs.core.get.call(null,map__30739__$1,new cljs.core.Keyword(null,"y-range","y-range",205237097),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(200)], null));
var data_chan = cljs.core.get.call(null,map__30739__$1,new cljs.core.Keyword(null,"data-chan","data-chan",-55900851));
if((data_chan == null)){
throw (new Error("XY Plot requires a data channel!"));
} else {
cljs.core.reset_BANG_.call(null,geom_om.xy.chart_data_chan,data_chan);
}

cljs.core.reset_BANG_.call(null,geom_om.xy.chart_width,width);

cljs.core.reset_BANG_.call(null,geom_om.xy.chart_height,height);

cljs.core.reset_BANG_.call(null,geom_om.xy.chart_x_range,x_range);

cljs.core.reset_BANG_.call(null,geom_om.xy.chart_y_range,y_range);

return ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function (cursor,owner){
if(typeof geom_om.xy.t30740 !== 'undefined'){
} else {

/**
* @constructor
*/
geom_om.xy.t30740 = (function (owner,x_range,height,p__30734,y_range,width,cursor,map__30739,data_chan,chart,meta30741){
this.owner = owner;
this.x_range = x_range;
this.height = height;
this.p__30734 = p__30734;
this.y_range = y_range;
this.width = width;
this.cursor = cursor;
this.map__30739 = map__30739;
this.data_chan = data_chan;
this.chart = chart;
this.meta30741 = meta30741;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
geom_om.xy.t30740.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function (_30742,meta30741__$1){
var self__ = this;
var _30742__$1 = this;
return (new geom_om.xy.t30740(self__.owner,self__.x_range,self__.height,self__.p__30734,self__.y_range,self__.width,self__.cursor,self__.map__30739,self__.data_chan,self__.chart,meta30741__$1));
});})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
;

geom_om.xy.t30740.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function (_30742){
var self__ = this;
var _30742__$1 = this;
return self__.meta30741;
});})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
;

geom_om.xy.t30740.prototype.om$core$IWillMount$ = true;

geom_om.xy.t30740.prototype.om$core$IWillMount$will_mount$arity$1 = ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function (_){
var self__ = this;
var ___$1 = this;
return geom_om.xy.data_loop.call(null,self__.cursor,cljs.core.deref.call(null,geom_om.xy.chart_data_chan));
});})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
;

geom_om.xy.t30740.prototype.om$core$IRender$ = true;

geom_om.xy.t30740.prototype.om$core$IRender$render$arity$1 = ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function (_){
var self__ = this;
var ___$1 = this;
return React.DOM.div({"dangerouslySetInnerHTML": {"__html": [cljs.core.str(hiccups.runtime.render_html.call(null,thi.ng.geom.svg.core.svg.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"width","width",-384071477),cljs.core.deref.call(null,geom_om.xy.chart_width),new cljs.core.Keyword(null,"height","height",1025178622),cljs.core.deref.call(null,geom_om.xy.chart_height)], null),thi.ng.geom.viz.core.svg_plot2d_cartesian.call(null,new cljs.core.Keyword(null,"element","element",1974019749).cljs$core$IFn$_invoke$arity$1(cljs.core.deref.call(null,self__.cursor))))))].join('')}});
});})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
;

geom_om.xy.t30740.getBasis = ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function (){
return new cljs.core.PersistentVector(null, 11, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"x-range","x-range",-833786076,null),new cljs.core.Symbol(null,"height","height",-1629257147,null),new cljs.core.Symbol(null,"p__30734","p__30734",1016949614,null),new cljs.core.Symbol(null,"y-range","y-range",1845768624,null),new cljs.core.Symbol(null,"width","width",1256460050,null),new cljs.core.Symbol(null,"cursor","cursor",-1642498285,null),new cljs.core.Symbol(null,"map__30739","map__30739",2065450963,null),new cljs.core.Symbol(null,"data-chan","data-chan",1584630676,null),new cljs.core.Symbol(null,"chart","chart",-1481210344,null),new cljs.core.Symbol(null,"meta30741","meta30741",-686469169,null)], null);
});})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
;

geom_om.xy.t30740.cljs$lang$type = true;

geom_om.xy.t30740.cljs$lang$ctorStr = "geom-om.xy/t30740";

geom_om.xy.t30740.cljs$lang$ctorPrWriter = ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"geom-om.xy/t30740");
});})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
;

geom_om.xy.__GT_t30740 = ((function (map__30739,map__30739__$1,width,height,x_range,y_range,data_chan){
return (function geom_om$xy$chart_$___GT_t30740(owner__$1,x_range__$1,height__$1,p__30734__$1,y_range__$1,width__$1,cursor__$1,map__30739__$2,data_chan__$1,chart__$1,meta30741){
return (new geom_om.xy.t30740(owner__$1,x_range__$1,height__$1,p__30734__$1,y_range__$1,width__$1,cursor__$1,map__30739__$2,data_chan__$1,chart__$1,meta30741));
});})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
;

}

return (new geom_om.xy.t30740(owner,x_range,height,p__30734,y_range,width,cursor,map__30739__$1,data_chan,geom_om$xy$chart,cljs.core.PersistentArrayMap.EMPTY));
});
;})(map__30739,map__30739__$1,width,height,x_range,y_range,data_chan))
});

//# sourceMappingURL=xy.js.map?rel=1439206034219
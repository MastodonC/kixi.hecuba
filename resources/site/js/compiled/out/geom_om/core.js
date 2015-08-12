// Compiled by ClojureScript 0.0-3297 {}
goog.provide('geom_om.core');
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
goog.require('geom_om.heatmap');
goog.require('thi.ng.geom.svg.core');
goog.require('thi.ng.math.core');
goog.require('cljs.core.async');
goog.require('geom_om.xy');
goog.require('goog.string.format');
goog.require('om.core');
goog.require('cljs.reader');
if(typeof geom_om.core.xy_data_chan !== 'undefined'){
} else {
geom_om.core.xy_data_chan = cljs.core.async.chan.call(null);
}
if(typeof geom_om.core.heatmap_data_chan !== 'undefined'){
} else {
geom_om.core.heatmap_data_chan = cljs.core.async.chan.call(null);
}
if(typeof geom_om.core.app_state !== 'undefined'){
} else {
geom_om.core.app_state = cljs.core.atom.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"xy","xy",-696978232),cljs.core.PersistentArrayMap.EMPTY,new cljs.core.Keyword(null,"heatmap","heatmap",-7865851),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"data","data",-232669377),null], null),new cljs.core.Keyword(null,"heatmap-controls","heatmap-controls",1440204573),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"lcb","lcb",1646475679),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"default","default",-1987822328),(10)], null),new cljs.core.Keyword(null,"ucb","ucb",19334455),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"default","default",-1987822328),(30)], null),new cljs.core.Keyword(null,"grads","grads",-33995815),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"default","default",-1987822328),(20)], null)], null)], null));
}
cljs.core.enable_console_print_BANG_.call(null);
var c__23633__auto___30784 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___30784){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___30784){
return (function (state_30774){
var state_val_30775 = (state_30774[(1)]);
if((state_val_30775 === (1))){
var inst_30745 = cljs_http.client.get.call(null,"/data/heatmap.edn");
var state_30774__$1 = state_30774;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_30774__$1,(2),inst_30745);
} else {
if((state_val_30775 === (2))){
var inst_30747 = (state_30774[(2)]);
var inst_30748 = new cljs.core.Keyword(null,"body","body",-2049205669).cljs$core$IFn$_invoke$arity$1(inst_30747);
var inst_30749 = new cljs.core.Keyword(null,"data","data",-232669377).cljs$core$IFn$_invoke$arity$1(inst_30748);
var inst_30750 = cljs.core.map.call(null,new cljs.core.Keyword(null,"value","value",305978217),inst_30749);
var inst_30751 = cljs.core.apply.call(null,cljs.core.min,inst_30750);
var inst_30752 = Math.floor(inst_30751);
var inst_30753 = cljs.core.apply.call(null,cljs.core.max,inst_30750);
var inst_30754 = Math.ceil(inst_30753);
var inst_30755 = om.core.root_cursor.call(null,geom_om.core.app_state);
var inst_30756 = cljs.core.PersistentVector.EMPTY_NODE;
var inst_30757 = [new cljs.core.Keyword(null,"heatmap-controls","heatmap-controls",1440204573),new cljs.core.Keyword(null,"lcb","lcb",1646475679),new cljs.core.Keyword(null,"default","default",-1987822328)];
var inst_30758 = (new cljs.core.PersistentVector(null,3,(5),inst_30756,inst_30757,null));
var inst_30759 = om.core.update_BANG_.call(null,inst_30755,inst_30758,inst_30752);
var inst_30760 = om.core.root_cursor.call(null,geom_om.core.app_state);
var inst_30761 = cljs.core.PersistentVector.EMPTY_NODE;
var inst_30762 = [new cljs.core.Keyword(null,"heatmap-controls","heatmap-controls",1440204573),new cljs.core.Keyword(null,"ucb","ucb",19334455),new cljs.core.Keyword(null,"default","default",-1987822328)];
var inst_30763 = (new cljs.core.PersistentVector(null,3,(5),inst_30761,inst_30762,null));
var inst_30764 = om.core.update_BANG_.call(null,inst_30760,inst_30763,inst_30754);
var inst_30765 = [new cljs.core.Keyword(null,"data","data",-232669377),new cljs.core.Keyword(null,"lcb","lcb",1646475679),new cljs.core.Keyword(null,"ucb","ucb",19334455),new cljs.core.Keyword(null,"grads","grads",-33995815)];
var inst_30766 = cljs.core.deref.call(null,geom_om.core.app_state);
var inst_30767 = new cljs.core.Keyword(null,"heatmap-controls","heatmap-controls",1440204573).cljs$core$IFn$_invoke$arity$1(inst_30766);
var inst_30768 = new cljs.core.Keyword(null,"grads","grads",-33995815).cljs$core$IFn$_invoke$arity$1(inst_30767);
var inst_30769 = new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(inst_30768);
var inst_30770 = [inst_30750,inst_30752,inst_30754,inst_30769];
var inst_30771 = cljs.core.PersistentHashMap.fromArrays(inst_30765,inst_30770);
var inst_30772 = cljs.core.async.put_BANG_.call(null,geom_om.core.heatmap_data_chan,inst_30771);
var state_30774__$1 = (function (){var statearr_30776 = state_30774;
(statearr_30776[(7)] = inst_30764);

(statearr_30776[(8)] = inst_30759);

return statearr_30776;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_30774__$1,inst_30772);
} else {
return null;
}
}
});})(c__23633__auto___30784))
;
return ((function (switch__23571__auto__,c__23633__auto___30784){
return (function() {
var geom_om$core$state_machine__23572__auto__ = null;
var geom_om$core$state_machine__23572__auto____0 = (function (){
var statearr_30780 = [null,null,null,null,null,null,null,null,null];
(statearr_30780[(0)] = geom_om$core$state_machine__23572__auto__);

(statearr_30780[(1)] = (1));

return statearr_30780;
});
var geom_om$core$state_machine__23572__auto____1 = (function (state_30774){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_30774);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e30781){if((e30781 instanceof Object)){
var ex__23575__auto__ = e30781;
var statearr_30782_30785 = state_30774;
(statearr_30782_30785[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_30774);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e30781;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__30786 = state_30774;
state_30774 = G__30786;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
geom_om$core$state_machine__23572__auto__ = function(state_30774){
switch(arguments.length){
case 0:
return geom_om$core$state_machine__23572__auto____0.call(this);
case 1:
return geom_om$core$state_machine__23572__auto____1.call(this,state_30774);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
geom_om$core$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = geom_om$core$state_machine__23572__auto____0;
geom_om$core$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = geom_om$core$state_machine__23572__auto____1;
return geom_om$core$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___30784))
})();
var state__23635__auto__ = (function (){var statearr_30783 = f__23634__auto__.call(null);
(statearr_30783[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___30784);

return statearr_30783;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___30784))
);

om.core.root.call(null,geom_om.heatmap.chart.call(null,new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"width","width",-384071477),(800),new cljs.core.Keyword(null,"height","height",1025178622),(600),new cljs.core.Keyword(null,"data-chan","data-chan",-55900851),geom_om.core.heatmap_data_chan,new cljs.core.Keyword(null,"fill-out-of-range-cells?","fill-out-of-range-cells?",775970238),false], null)),geom_om.core.app_state,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"target","target",253001721),document.getElementById("heatmap"),new cljs.core.Keyword(null,"path","path",-188191168),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"heatmap","heatmap",-7865851)], null)], null));
geom_om.core.update_chart_settings = (function geom_om$core$update_chart_settings(owner,cursor){
var raw_lcb_30787 = cljs.reader.read_string.call(null,om.core.get_node.call(null,owner,"lcb-input").value);
var raw_ucb_30788 = cljs.reader.read_string.call(null,om.core.get_node.call(null,owner,"ucb-input").value);
var raw_grads_30789 = cljs.reader.read_string.call(null,om.core.get_node.call(null,owner,"grads-input").value);
var lcb_30790 = (((raw_lcb_30787 == null))?new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"lcb","lcb",1646475679).cljs$core$IFn$_invoke$arity$1(cursor)):raw_lcb_30787);
var ucb_30791 = (((raw_ucb_30788 == null))?new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"ucb","ucb",19334455).cljs$core$IFn$_invoke$arity$1(cursor)):raw_ucb_30788);
var grads_30792 = (((raw_grads_30789 == null))?new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"grads","grads",-33995815).cljs$core$IFn$_invoke$arity$1(cursor)):raw_grads_30789);
cljs.core.async.put_BANG_.call(null,geom_om.core.heatmap_data_chan,new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"data","data",-232669377),new cljs.core.Keyword(null,"data","data",-232669377).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"heatmap","heatmap",-7865851).cljs$core$IFn$_invoke$arity$1(cljs.core.deref.call(null,geom_om.core.app_state))),new cljs.core.Keyword(null,"lcb","lcb",1646475679),lcb_30790,new cljs.core.Keyword(null,"ucb","ucb",19334455),ucb_30791,new cljs.core.Keyword(null,"grads","grads",-33995815),grads_30792], null));

return null;
});
om.core.root.call(null,(function (cursor,owner){
if(typeof geom_om.core.t30793 !== 'undefined'){
} else {

/**
* @constructor
*/
geom_om.core.t30793 = (function (cursor,owner,meta30794){
this.cursor = cursor;
this.owner = owner;
this.meta30794 = meta30794;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
geom_om.core.t30793.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_30795,meta30794__$1){
var self__ = this;
var _30795__$1 = this;
return (new geom_om.core.t30793(self__.cursor,self__.owner,meta30794__$1));
});

geom_om.core.t30793.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_30795){
var self__ = this;
var _30795__$1 = this;
return self__.meta30794;
});

geom_om.core.t30793.prototype.om$core$IRender$ = true;

geom_om.core.t30793.prototype.om$core$IRender$render$arity$1 = (function (this__28351__auto__){
var self__ = this;
var this__28351__auto____$1 = this;
return React.DOM.div(null,React.DOM.span(null,"Lower colour bound"),om.dom.input.call(null,{"ref": "lcb-input", "placeholder": new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"lcb","lcb",1646475679).cljs$core$IFn$_invoke$arity$1(self__.cursor))}),React.DOM.span(null,"Upper colour bound"),om.dom.input.call(null,{"ref": "ucb-input", "placeholder": new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"ucb","ucb",19334455).cljs$core$IFn$_invoke$arity$1(self__.cursor))}),React.DOM.span(null,"Gradations"),om.dom.input.call(null,{"ref": "grads-input", "placeholder": new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"grads","grads",-33995815).cljs$core$IFn$_invoke$arity$1(self__.cursor))}),React.DOM.button({"onClick": ((function (this__28351__auto____$1){
return (function (){
return geom_om.core.update_chart_settings.call(null,self__.owner,self__.cursor);
});})(this__28351__auto____$1))
},"Refresh"));
});

geom_om.core.t30793.getBasis = (function (){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"cursor","cursor",-1642498285,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"meta30794","meta30794",-893798459,null)], null);
});

geom_om.core.t30793.cljs$lang$type = true;

geom_om.core.t30793.cljs$lang$ctorStr = "geom-om.core/t30793";

geom_om.core.t30793.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"geom-om.core/t30793");
});

geom_om.core.__GT_t30793 = (function geom_om$core$__GT_t30793(cursor__$1,owner__$1,meta30794){
return (new geom_om.core.t30793(cursor__$1,owner__$1,meta30794));
});

}

return (new geom_om.core.t30793(cursor,owner,null));
}),geom_om.core.app_state,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"target","target",253001721),document.getElementById("heatmap-controls"),new cljs.core.Keyword(null,"path","path",-188191168),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"heatmap-controls","heatmap-controls",1440204573)], null)], null));
var c__23633__auto___30812 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___30812){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___30812){
return (function (state_30803){
var state_val_30804 = (state_30803[(1)]);
if((state_val_30804 === (1))){
var inst_30796 = cljs_http.client.get.call(null,"/data/xyplot.edn");
var state_30803__$1 = state_30803;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_30803__$1,(2),inst_30796);
} else {
if((state_val_30804 === (2))){
var inst_30798 = (state_30803[(2)]);
var inst_30799 = new cljs.core.Keyword(null,"body","body",-2049205669).cljs$core$IFn$_invoke$arity$1(inst_30798);
var inst_30800 = new cljs.core.Keyword(null,"data","data",-232669377).cljs$core$IFn$_invoke$arity$1(inst_30799);
var inst_30801 = cljs.core.async.put_BANG_.call(null,geom_om.core.xy_data_chan,inst_30800);
var state_30803__$1 = state_30803;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_30803__$1,inst_30801);
} else {
return null;
}
}
});})(c__23633__auto___30812))
;
return ((function (switch__23571__auto__,c__23633__auto___30812){
return (function() {
var geom_om$core$state_machine__23572__auto__ = null;
var geom_om$core$state_machine__23572__auto____0 = (function (){
var statearr_30808 = [null,null,null,null,null,null,null];
(statearr_30808[(0)] = geom_om$core$state_machine__23572__auto__);

(statearr_30808[(1)] = (1));

return statearr_30808;
});
var geom_om$core$state_machine__23572__auto____1 = (function (state_30803){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_30803);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e30809){if((e30809 instanceof Object)){
var ex__23575__auto__ = e30809;
var statearr_30810_30813 = state_30803;
(statearr_30810_30813[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_30803);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e30809;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__30814 = state_30803;
state_30803 = G__30814;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
geom_om$core$state_machine__23572__auto__ = function(state_30803){
switch(arguments.length){
case 0:
return geom_om$core$state_machine__23572__auto____0.call(this);
case 1:
return geom_om$core$state_machine__23572__auto____1.call(this,state_30803);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
geom_om$core$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = geom_om$core$state_machine__23572__auto____0;
geom_om$core$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = geom_om$core$state_machine__23572__auto____1;
return geom_om$core$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___30812))
})();
var state__23635__auto__ = (function (){var statearr_30811 = f__23634__auto__.call(null);
(statearr_30811[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___30812);

return statearr_30811;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___30812))
);

om.core.root.call(null,geom_om.xy.chart.call(null,new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"width","width",-384071477),(800),new cljs.core.Keyword(null,"height","height",1025178622),(600),new cljs.core.Keyword(null,"x-range","x-range",1820649693),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(200)], null),new cljs.core.Keyword(null,"y-range","y-range",205237097),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(200)], null),new cljs.core.Keyword(null,"data-chan","data-chan",-55900851),geom_om.core.xy_data_chan], null)),geom_om.core.app_state,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"target","target",253001721),document.getElementById("xy-plot"),new cljs.core.Keyword(null,"path","path",-188191168),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"xy","xy",-696978232)], null)], null));
geom_om.core.on_js_reload = (function geom_om$core$on_js_reload(){
return null;
});

//# sourceMappingURL=core.js.map?rel=1439206034300
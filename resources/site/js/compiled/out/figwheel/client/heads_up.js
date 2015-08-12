// Compiled by ClojureScript 0.0-3297 {}
goog.provide('figwheel.client.heads_up');
goog.require('cljs.core');
goog.require('clojure.string');
goog.require('figwheel.client.socket');
goog.require('cljs.core.async');
goog.require('goog.string');

figwheel.client.heads_up.node = (function figwheel$client$heads_up$node(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return figwheel.client.heads_up.node.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

figwheel.client.heads_up.node.cljs$core$IFn$_invoke$arity$variadic = (function (t,attrs,children){
var e = document.createElement(cljs.core.name.call(null,t));
var seq__42016_42024 = cljs.core.seq.call(null,cljs.core.keys.call(null,attrs));
var chunk__42017_42025 = null;
var count__42018_42026 = (0);
var i__42019_42027 = (0);
while(true){
if((i__42019_42027 < count__42018_42026)){
var k_42028 = cljs.core._nth.call(null,chunk__42017_42025,i__42019_42027);
e.setAttribute(cljs.core.name.call(null,k_42028),cljs.core.get.call(null,attrs,k_42028));

var G__42029 = seq__42016_42024;
var G__42030 = chunk__42017_42025;
var G__42031 = count__42018_42026;
var G__42032 = (i__42019_42027 + (1));
seq__42016_42024 = G__42029;
chunk__42017_42025 = G__42030;
count__42018_42026 = G__42031;
i__42019_42027 = G__42032;
continue;
} else {
var temp__4425__auto___42033 = cljs.core.seq.call(null,seq__42016_42024);
if(temp__4425__auto___42033){
var seq__42016_42034__$1 = temp__4425__auto___42033;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__42016_42034__$1)){
var c__16854__auto___42035 = cljs.core.chunk_first.call(null,seq__42016_42034__$1);
var G__42036 = cljs.core.chunk_rest.call(null,seq__42016_42034__$1);
var G__42037 = c__16854__auto___42035;
var G__42038 = cljs.core.count.call(null,c__16854__auto___42035);
var G__42039 = (0);
seq__42016_42024 = G__42036;
chunk__42017_42025 = G__42037;
count__42018_42026 = G__42038;
i__42019_42027 = G__42039;
continue;
} else {
var k_42040 = cljs.core.first.call(null,seq__42016_42034__$1);
e.setAttribute(cljs.core.name.call(null,k_42040),cljs.core.get.call(null,attrs,k_42040));

var G__42041 = cljs.core.next.call(null,seq__42016_42034__$1);
var G__42042 = null;
var G__42043 = (0);
var G__42044 = (0);
seq__42016_42024 = G__42041;
chunk__42017_42025 = G__42042;
count__42018_42026 = G__42043;
i__42019_42027 = G__42044;
continue;
}
} else {
}
}
break;
}

var seq__42020_42045 = cljs.core.seq.call(null,children);
var chunk__42021_42046 = null;
var count__42022_42047 = (0);
var i__42023_42048 = (0);
while(true){
if((i__42023_42048 < count__42022_42047)){
var ch_42049 = cljs.core._nth.call(null,chunk__42021_42046,i__42023_42048);
e.appendChild(ch_42049);

var G__42050 = seq__42020_42045;
var G__42051 = chunk__42021_42046;
var G__42052 = count__42022_42047;
var G__42053 = (i__42023_42048 + (1));
seq__42020_42045 = G__42050;
chunk__42021_42046 = G__42051;
count__42022_42047 = G__42052;
i__42023_42048 = G__42053;
continue;
} else {
var temp__4425__auto___42054 = cljs.core.seq.call(null,seq__42020_42045);
if(temp__4425__auto___42054){
var seq__42020_42055__$1 = temp__4425__auto___42054;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__42020_42055__$1)){
var c__16854__auto___42056 = cljs.core.chunk_first.call(null,seq__42020_42055__$1);
var G__42057 = cljs.core.chunk_rest.call(null,seq__42020_42055__$1);
var G__42058 = c__16854__auto___42056;
var G__42059 = cljs.core.count.call(null,c__16854__auto___42056);
var G__42060 = (0);
seq__42020_42045 = G__42057;
chunk__42021_42046 = G__42058;
count__42022_42047 = G__42059;
i__42023_42048 = G__42060;
continue;
} else {
var ch_42061 = cljs.core.first.call(null,seq__42020_42055__$1);
e.appendChild(ch_42061);

var G__42062 = cljs.core.next.call(null,seq__42020_42055__$1);
var G__42063 = null;
var G__42064 = (0);
var G__42065 = (0);
seq__42020_42045 = G__42062;
chunk__42021_42046 = G__42063;
count__42022_42047 = G__42064;
i__42023_42048 = G__42065;
continue;
}
} else {
}
}
break;
}

return e;
});

figwheel.client.heads_up.node.cljs$lang$maxFixedArity = (2);

figwheel.client.heads_up.node.cljs$lang$applyTo = (function (seq42013){
var G__42014 = cljs.core.first.call(null,seq42013);
var seq42013__$1 = cljs.core.next.call(null,seq42013);
var G__42015 = cljs.core.first.call(null,seq42013__$1);
var seq42013__$2 = cljs.core.next.call(null,seq42013__$1);
return figwheel.client.heads_up.node.cljs$core$IFn$_invoke$arity$variadic(G__42014,G__42015,seq42013__$2);
});
if(typeof figwheel.client.heads_up.heads_up_event_dispatch !== 'undefined'){
} else {
figwheel.client.heads_up.heads_up_event_dispatch = (function (){var method_table__16964__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var prefer_table__16965__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var method_cache__16966__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var cached_hierarchy__16967__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var hierarchy__16968__auto__ = cljs.core.get.call(null,cljs.core.PersistentArrayMap.EMPTY,new cljs.core.Keyword(null,"hierarchy","hierarchy",-1053470341),cljs.core.get_global_hierarchy.call(null));
return (new cljs.core.MultiFn(cljs.core.symbol.call(null,"figwheel.client.heads-up","heads-up-event-dispatch"),((function (method_table__16964__auto__,prefer_table__16965__auto__,method_cache__16966__auto__,cached_hierarchy__16967__auto__,hierarchy__16968__auto__){
return (function (dataset){
return dataset.figwheelEvent;
});})(method_table__16964__auto__,prefer_table__16965__auto__,method_cache__16966__auto__,cached_hierarchy__16967__auto__,hierarchy__16968__auto__))
,new cljs.core.Keyword(null,"default","default",-1987822328),hierarchy__16968__auto__,method_table__16964__auto__,prefer_table__16965__auto__,method_cache__16966__auto__,cached_hierarchy__16967__auto__));
})();
}
cljs.core._add_method.call(null,figwheel.client.heads_up.heads_up_event_dispatch,new cljs.core.Keyword(null,"default","default",-1987822328),(function (_){
return cljs.core.PersistentArrayMap.EMPTY;
}));
cljs.core._add_method.call(null,figwheel.client.heads_up.heads_up_event_dispatch,"file-selected",(function (dataset){
return figwheel.client.socket.send_BANG_.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"figwheel-event","figwheel-event",519570592),"file-selected",new cljs.core.Keyword(null,"file-name","file-name",-1654217259),dataset.fileName,new cljs.core.Keyword(null,"file-line","file-line",-1228823138),dataset.fileLine], null));
}));
cljs.core._add_method.call(null,figwheel.client.heads_up.heads_up_event_dispatch,"close-heads-up",(function (dataset){
return figwheel.client.heads_up.clear.call(null);
}));
figwheel.client.heads_up.ancestor_nodes = (function figwheel$client$heads_up$ancestor_nodes(el){
return cljs.core.iterate.call(null,(function (e){
return e.parentNode;
}),el);
});
figwheel.client.heads_up.get_dataset = (function figwheel$client$heads_up$get_dataset(el){
return cljs.core.first.call(null,cljs.core.keep.call(null,(function (x){
if(cljs.core.truth_(x.dataset.figwheelEvent)){
return x.dataset;
} else {
return null;
}
}),cljs.core.take.call(null,(4),figwheel.client.heads_up.ancestor_nodes.call(null,el))));
});
figwheel.client.heads_up.heads_up_onclick_handler = (function figwheel$client$heads_up$heads_up_onclick_handler(event){
var dataset = figwheel.client.heads_up.get_dataset.call(null,event.target);
event.preventDefault();

if(cljs.core.truth_(dataset)){
return figwheel.client.heads_up.heads_up_event_dispatch.call(null,dataset);
} else {
return null;
}
});
figwheel.client.heads_up.ensure_container = (function figwheel$client$heads_up$ensure_container(){
var cont_id = "figwheel-heads-up-container";
var content_id = "figwheel-heads-up-content-area";
if(cljs.core.not.call(null,document.querySelector([cljs.core.str("#"),cljs.core.str(cont_id)].join('')))){
var el_42066 = figwheel.client.heads_up.node.call(null,new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"id","id",-1388402092),cont_id,new cljs.core.Keyword(null,"style","style",-496642736),[cljs.core.str("-webkit-transition: all 0.2s ease-in-out;"),cljs.core.str("-moz-transition: all 0.2s ease-in-out;"),cljs.core.str("-o-transition: all 0.2s ease-in-out;"),cljs.core.str("transition: all 0.2s ease-in-out;"),cljs.core.str("font-size: 13px;"),cljs.core.str("border-top: 1px solid #f5f5f5;"),cljs.core.str("box-shadow: 0px 0px 1px #aaaaaa;"),cljs.core.str("line-height: 18px;"),cljs.core.str("color: #333;"),cljs.core.str("font-family: monospace;"),cljs.core.str("padding: 0px 10px 0px 70px;"),cljs.core.str("position: fixed;"),cljs.core.str("bottom: 0px;"),cljs.core.str("left: 0px;"),cljs.core.str("height: 0px;"),cljs.core.str("opacity: 0.0;"),cljs.core.str("box-sizing: border-box;"),cljs.core.str("z-index: 10000;")].join('')], null));
el_42066.onclick = figwheel.client.heads_up.heads_up_onclick_handler;

el_42066.innerHTML = [cljs.core.str(figwheel.client.heads_up.clojure_symbol_svg)].join('');

el_42066.appendChild(figwheel.client.heads_up.node.call(null,new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"id","id",-1388402092),content_id], null)));

document.body.appendChild(el_42066);
} else {
}

return new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"container-el","container-el",109664205),document.getElementById(cont_id),new cljs.core.Keyword(null,"content-area-el","content-area-el",742757187),document.getElementById(content_id)], null);
});
figwheel.client.heads_up.set_style_BANG_ = (function figwheel$client$heads_up$set_style_BANG_(p__42067,st_map){
var map__42071 = p__42067;
var map__42071__$1 = ((cljs.core.seq_QMARK_.call(null,map__42071))?cljs.core.apply.call(null,cljs.core.hash_map,map__42071):map__42071);
var container_el = cljs.core.get.call(null,map__42071__$1,new cljs.core.Keyword(null,"container-el","container-el",109664205));
return cljs.core.mapv.call(null,((function (map__42071,map__42071__$1,container_el){
return (function (p__42072){
var vec__42073 = p__42072;
var k = cljs.core.nth.call(null,vec__42073,(0),null);
var v = cljs.core.nth.call(null,vec__42073,(1),null);
return (container_el.style[cljs.core.name.call(null,k)] = v);
});})(map__42071,map__42071__$1,container_el))
,st_map);
});
figwheel.client.heads_up.set_content_BANG_ = (function figwheel$client$heads_up$set_content_BANG_(p__42074,dom_str){
var map__42076 = p__42074;
var map__42076__$1 = ((cljs.core.seq_QMARK_.call(null,map__42076))?cljs.core.apply.call(null,cljs.core.hash_map,map__42076):map__42076);
var c = map__42076__$1;
var content_area_el = cljs.core.get.call(null,map__42076__$1,new cljs.core.Keyword(null,"content-area-el","content-area-el",742757187));
return content_area_el.innerHTML = dom_str;
});
figwheel.client.heads_up.get_content = (function figwheel$client$heads_up$get_content(p__42077){
var map__42079 = p__42077;
var map__42079__$1 = ((cljs.core.seq_QMARK_.call(null,map__42079))?cljs.core.apply.call(null,cljs.core.hash_map,map__42079):map__42079);
var content_area_el = cljs.core.get.call(null,map__42079__$1,new cljs.core.Keyword(null,"content-area-el","content-area-el",742757187));
return content_area_el.innerHTML;
});
figwheel.client.heads_up.close_link = (function figwheel$client$heads_up$close_link(){
return [cljs.core.str("<a style=\""),cljs.core.str("float: right;"),cljs.core.str("font-size: 18px;"),cljs.core.str("text-decoration: none;"),cljs.core.str("text-align: right;"),cljs.core.str("width: 30px;"),cljs.core.str("height: 30px;"),cljs.core.str("color: rgba(84,84,84, 0.5);"),cljs.core.str("\" href=\"#\"  data-figwheel-event=\"close-heads-up\">"),cljs.core.str("x"),cljs.core.str("</a>")].join('');
});
figwheel.client.heads_up.display_heads_up = (function figwheel$client$heads_up$display_heads_up(style,msg){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_42121){
var state_val_42122 = (state_42121[(1)]);
if((state_val_42122 === (1))){
var inst_42106 = (state_42121[(7)]);
var inst_42106__$1 = figwheel.client.heads_up.ensure_container.call(null);
var inst_42107 = [new cljs.core.Keyword(null,"paddingTop","paddingTop",-1088692345),new cljs.core.Keyword(null,"paddingBottom","paddingBottom",-916694489),new cljs.core.Keyword(null,"width","width",-384071477),new cljs.core.Keyword(null,"minHeight","minHeight",-1635998980),new cljs.core.Keyword(null,"opacity","opacity",397153780)];
var inst_42108 = ["10px","10px","100%","68px","1.0"];
var inst_42109 = cljs.core.PersistentHashMap.fromArrays(inst_42107,inst_42108);
var inst_42110 = cljs.core.merge.call(null,inst_42109,style);
var inst_42111 = figwheel.client.heads_up.set_style_BANG_.call(null,inst_42106__$1,inst_42110);
var inst_42112 = figwheel.client.heads_up.set_content_BANG_.call(null,inst_42106__$1,msg);
var inst_42113 = cljs.core.async.timeout.call(null,(300));
var state_42121__$1 = (function (){var statearr_42123 = state_42121;
(statearr_42123[(8)] = inst_42111);

(statearr_42123[(7)] = inst_42106__$1);

(statearr_42123[(9)] = inst_42112);

return statearr_42123;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42121__$1,(2),inst_42113);
} else {
if((state_val_42122 === (2))){
var inst_42106 = (state_42121[(7)]);
var inst_42115 = (state_42121[(2)]);
var inst_42116 = [new cljs.core.Keyword(null,"height","height",1025178622)];
var inst_42117 = ["auto"];
var inst_42118 = cljs.core.PersistentHashMap.fromArrays(inst_42116,inst_42117);
var inst_42119 = figwheel.client.heads_up.set_style_BANG_.call(null,inst_42106,inst_42118);
var state_42121__$1 = (function (){var statearr_42124 = state_42121;
(statearr_42124[(10)] = inst_42115);

return statearr_42124;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_42121__$1,inst_42119);
} else {
return null;
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto__ = null;
var figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto____0 = (function (){
var statearr_42128 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_42128[(0)] = figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto__);

(statearr_42128[(1)] = (1));

return statearr_42128;
});
var figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto____1 = (function (state_42121){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_42121);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e42129){if((e42129 instanceof Object)){
var ex__23575__auto__ = e42129;
var statearr_42130_42132 = state_42121;
(statearr_42130_42132[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_42121);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e42129;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__42133 = state_42121;
state_42121 = G__42133;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto__ = function(state_42121){
switch(arguments.length){
case 0:
return figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto____1.call(this,state_42121);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto____0;
figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto____1;
return figwheel$client$heads_up$display_heads_up_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_42131 = f__23634__auto__.call(null);
(statearr_42131[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_42131;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});
figwheel.client.heads_up.heading = (function figwheel$client$heads_up$heading(s){
return [cljs.core.str("<div style=\""),cljs.core.str("font-size: 26px;"),cljs.core.str("line-height: 26px;"),cljs.core.str("margin-bottom: 2px;"),cljs.core.str("padding-top: 1px;"),cljs.core.str("\">"),cljs.core.str(s),cljs.core.str("</div>")].join('');
});
figwheel.client.heads_up.file_and_line_number = (function figwheel$client$heads_up$file_and_line_number(msg){
if(cljs.core.truth_(cljs.core.re_matches.call(null,/.*at\sline.*/,msg))){
return cljs.core.take.call(null,(2),cljs.core.reverse.call(null,clojure.string.split.call(null,msg," ")));
} else {
return null;
}
});
figwheel.client.heads_up.file_selector_div = (function figwheel$client$heads_up$file_selector_div(file_name,line_number,msg){
return [cljs.core.str("<div data-figwheel-event=\"file-selected\" data-file-name=\""),cljs.core.str(file_name),cljs.core.str("\" data-file-line=\""),cljs.core.str(line_number),cljs.core.str("\">"),cljs.core.str(msg),cljs.core.str("</div>")].join('');
});
figwheel.client.heads_up.format_line = (function figwheel$client$heads_up$format_line(msg){
var msg__$1 = goog.string.htmlEscape(msg);
var temp__4423__auto__ = figwheel.client.heads_up.file_and_line_number.call(null,msg__$1);
if(cljs.core.truth_(temp__4423__auto__)){
var vec__42135 = temp__4423__auto__;
var f = cljs.core.nth.call(null,vec__42135,(0),null);
var ln = cljs.core.nth.call(null,vec__42135,(1),null);
return figwheel.client.heads_up.file_selector_div.call(null,f,ln,msg__$1);
} else {
return [cljs.core.str("<div>"),cljs.core.str(msg__$1),cljs.core.str("</div>")].join('');
}
});
figwheel.client.heads_up.display_error = (function figwheel$client$heads_up$display_error(formatted_messages,cause){
var vec__42138 = (cljs.core.truth_(cause)?new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"file","file",-1269645878).cljs$core$IFn$_invoke$arity$1(cause),new cljs.core.Keyword(null,"line","line",212345235).cljs$core$IFn$_invoke$arity$1(cause),new cljs.core.Keyword(null,"column","column",2078222095).cljs$core$IFn$_invoke$arity$1(cause)], null):cljs.core.first.call(null,cljs.core.keep.call(null,figwheel.client.heads_up.file_and_line_number,formatted_messages)));
var file_name = cljs.core.nth.call(null,vec__42138,(0),null);
var file_line = cljs.core.nth.call(null,vec__42138,(1),null);
var file_column = cljs.core.nth.call(null,vec__42138,(2),null);
var msg = cljs.core.apply.call(null,cljs.core.str,cljs.core.map.call(null,((function (vec__42138,file_name,file_line,file_column){
return (function (p1__42136_SHARP_){
return [cljs.core.str("<div>"),cljs.core.str(goog.string.htmlEscape(p1__42136_SHARP_)),cljs.core.str("</div>")].join('');
});})(vec__42138,file_name,file_line,file_column))
,formatted_messages));
return figwheel.client.heads_up.display_heads_up.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"backgroundColor","backgroundColor",1738438491),"rgba(255, 161, 161, 0.95)"], null),[cljs.core.str(figwheel.client.heads_up.close_link.call(null)),cljs.core.str(figwheel.client.heads_up.heading.call(null,"Compile Error")),cljs.core.str(figwheel.client.heads_up.file_selector_div.call(null,file_name,(function (){var or__16069__auto__ = file_line;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
var and__16057__auto__ = cause;
if(cljs.core.truth_(and__16057__auto__)){
return new cljs.core.Keyword(null,"line","line",212345235).cljs$core$IFn$_invoke$arity$1(cause);
} else {
return and__16057__auto__;
}
}
})(),[cljs.core.str(msg),cljs.core.str((cljs.core.truth_(cause)?[cljs.core.str("Error on file "),cljs.core.str(goog.string.htmlEscape(new cljs.core.Keyword(null,"file","file",-1269645878).cljs$core$IFn$_invoke$arity$1(cause))),cljs.core.str(", line "),cljs.core.str(new cljs.core.Keyword(null,"line","line",212345235).cljs$core$IFn$_invoke$arity$1(cause)),cljs.core.str(", column "),cljs.core.str(new cljs.core.Keyword(null,"column","column",2078222095).cljs$core$IFn$_invoke$arity$1(cause))].join(''):""))].join('')))].join(''));
});
figwheel.client.heads_up.display_system_warning = (function figwheel$client$heads_up$display_system_warning(header,msg){
return figwheel.client.heads_up.display_heads_up.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"backgroundColor","backgroundColor",1738438491),"rgba(255, 220, 110, 0.95)"], null),[cljs.core.str(figwheel.client.heads_up.close_link.call(null)),cljs.core.str(figwheel.client.heads_up.heading.call(null,header)),cljs.core.str(figwheel.client.heads_up.format_line.call(null,msg))].join(''));
});
figwheel.client.heads_up.display_warning = (function figwheel$client$heads_up$display_warning(msg){
return figwheel.client.heads_up.display_system_warning.call(null,"Compile Warning",msg);
});
figwheel.client.heads_up.append_message = (function figwheel$client$heads_up$append_message(message){
var map__42140 = figwheel.client.heads_up.ensure_container.call(null);
var map__42140__$1 = ((cljs.core.seq_QMARK_.call(null,map__42140))?cljs.core.apply.call(null,cljs.core.hash_map,map__42140):map__42140);
var content_area_el = cljs.core.get.call(null,map__42140__$1,new cljs.core.Keyword(null,"content-area-el","content-area-el",742757187));
var el = document.createElement("div");
el.innerHTML = figwheel.client.heads_up.format_line.call(null,message);

return content_area_el.appendChild(el);
});
figwheel.client.heads_up.clear = (function figwheel$client$heads_up$clear(){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_42187){
var state_val_42188 = (state_42187[(1)]);
if((state_val_42188 === (1))){
var inst_42170 = (state_42187[(7)]);
var inst_42170__$1 = figwheel.client.heads_up.ensure_container.call(null);
var inst_42171 = [new cljs.core.Keyword(null,"opacity","opacity",397153780)];
var inst_42172 = ["0.0"];
var inst_42173 = cljs.core.PersistentHashMap.fromArrays(inst_42171,inst_42172);
var inst_42174 = figwheel.client.heads_up.set_style_BANG_.call(null,inst_42170__$1,inst_42173);
var inst_42175 = cljs.core.async.timeout.call(null,(300));
var state_42187__$1 = (function (){var statearr_42189 = state_42187;
(statearr_42189[(7)] = inst_42170__$1);

(statearr_42189[(8)] = inst_42174);

return statearr_42189;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42187__$1,(2),inst_42175);
} else {
if((state_val_42188 === (2))){
var inst_42170 = (state_42187[(7)]);
var inst_42177 = (state_42187[(2)]);
var inst_42178 = [new cljs.core.Keyword(null,"width","width",-384071477),new cljs.core.Keyword(null,"height","height",1025178622),new cljs.core.Keyword(null,"minHeight","minHeight",-1635998980),new cljs.core.Keyword(null,"padding","padding",1660304693),new cljs.core.Keyword(null,"borderRadius","borderRadius",-1505621083),new cljs.core.Keyword(null,"backgroundColor","backgroundColor",1738438491)];
var inst_42179 = ["auto","0px","0px","0px 10px 0px 70px","0px","transparent"];
var inst_42180 = cljs.core.PersistentHashMap.fromArrays(inst_42178,inst_42179);
var inst_42181 = figwheel.client.heads_up.set_style_BANG_.call(null,inst_42170,inst_42180);
var inst_42182 = cljs.core.async.timeout.call(null,(200));
var state_42187__$1 = (function (){var statearr_42190 = state_42187;
(statearr_42190[(9)] = inst_42181);

(statearr_42190[(10)] = inst_42177);

return statearr_42190;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42187__$1,(3),inst_42182);
} else {
if((state_val_42188 === (3))){
var inst_42170 = (state_42187[(7)]);
var inst_42184 = (state_42187[(2)]);
var inst_42185 = figwheel.client.heads_up.set_content_BANG_.call(null,inst_42170,"");
var state_42187__$1 = (function (){var statearr_42191 = state_42187;
(statearr_42191[(11)] = inst_42184);

return statearr_42191;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_42187__$1,inst_42185);
} else {
return null;
}
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var figwheel$client$heads_up$clear_$_state_machine__23572__auto__ = null;
var figwheel$client$heads_up$clear_$_state_machine__23572__auto____0 = (function (){
var statearr_42195 = [null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_42195[(0)] = figwheel$client$heads_up$clear_$_state_machine__23572__auto__);

(statearr_42195[(1)] = (1));

return statearr_42195;
});
var figwheel$client$heads_up$clear_$_state_machine__23572__auto____1 = (function (state_42187){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_42187);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e42196){if((e42196 instanceof Object)){
var ex__23575__auto__ = e42196;
var statearr_42197_42199 = state_42187;
(statearr_42197_42199[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_42187);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e42196;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__42200 = state_42187;
state_42187 = G__42200;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$heads_up$clear_$_state_machine__23572__auto__ = function(state_42187){
switch(arguments.length){
case 0:
return figwheel$client$heads_up$clear_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$heads_up$clear_$_state_machine__23572__auto____1.call(this,state_42187);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$heads_up$clear_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$heads_up$clear_$_state_machine__23572__auto____0;
figwheel$client$heads_up$clear_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$heads_up$clear_$_state_machine__23572__auto____1;
return figwheel$client$heads_up$clear_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_42198 = f__23634__auto__.call(null);
(statearr_42198[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_42198;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});
figwheel.client.heads_up.display_loaded_start = (function figwheel$client$heads_up$display_loaded_start(){
return figwheel.client.heads_up.display_heads_up.call(null,new cljs.core.PersistentArrayMap(null, 6, [new cljs.core.Keyword(null,"backgroundColor","backgroundColor",1738438491),"rgba(211,234,172,1.0)",new cljs.core.Keyword(null,"width","width",-384071477),"68px",new cljs.core.Keyword(null,"height","height",1025178622),"68px",new cljs.core.Keyword(null,"paddingLeft","paddingLeft",262720813),"0px",new cljs.core.Keyword(null,"paddingRight","paddingRight",-1642313463),"0px",new cljs.core.Keyword(null,"borderRadius","borderRadius",-1505621083),"35px"], null),"");
});
figwheel.client.heads_up.flash_loaded = (function figwheel$client$heads_up$flash_loaded(){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_42232){
var state_val_42233 = (state_42232[(1)]);
if((state_val_42233 === (1))){
var inst_42222 = figwheel.client.heads_up.display_loaded_start.call(null);
var state_42232__$1 = state_42232;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42232__$1,(2),inst_42222);
} else {
if((state_val_42233 === (2))){
var inst_42224 = (state_42232[(2)]);
var inst_42225 = cljs.core.async.timeout.call(null,(400));
var state_42232__$1 = (function (){var statearr_42234 = state_42232;
(statearr_42234[(7)] = inst_42224);

return statearr_42234;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42232__$1,(3),inst_42225);
} else {
if((state_val_42233 === (3))){
var inst_42227 = (state_42232[(2)]);
var inst_42228 = figwheel.client.heads_up.clear.call(null);
var state_42232__$1 = (function (){var statearr_42235 = state_42232;
(statearr_42235[(8)] = inst_42227);

return statearr_42235;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42232__$1,(4),inst_42228);
} else {
if((state_val_42233 === (4))){
var inst_42230 = (state_42232[(2)]);
var state_42232__$1 = state_42232;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_42232__$1,inst_42230);
} else {
return null;
}
}
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto__ = null;
var figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto____0 = (function (){
var statearr_42239 = [null,null,null,null,null,null,null,null,null];
(statearr_42239[(0)] = figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto__);

(statearr_42239[(1)] = (1));

return statearr_42239;
});
var figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto____1 = (function (state_42232){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_42232);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e42240){if((e42240 instanceof Object)){
var ex__23575__auto__ = e42240;
var statearr_42241_42243 = state_42232;
(statearr_42241_42243[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_42232);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e42240;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__42244 = state_42232;
state_42232 = G__42244;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto__ = function(state_42232){
switch(arguments.length){
case 0:
return figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto____1.call(this,state_42232);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto____0;
figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto____1;
return figwheel$client$heads_up$flash_loaded_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_42242 = f__23634__auto__.call(null);
(statearr_42242[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_42242;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});
figwheel.client.heads_up.clojure_symbol_svg = "<?xml version='1.0' encoding='UTF-8' ?>\n<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>\n<svg width='49px' height='49px' viewBox='0 0 100 99' version='1.1' xmlns='http://www.w3.org/2000/svg' style='position:absolute; top:9px; left: 10px;'>\n<circle fill='rgba(255,255,255,0.5)' cx='49.75' cy='49.5' r='48.5'/>\n<path fill='#5881d8' d=' M 39.30 6.22 C 51.71 3.11 65.45 5.64 75.83 13.16 C 88.68 22.10 96.12 38.22 94.43 53.80 C 93.66 60.11 89.40 66.01 83.37 68.24 C 79.21 69.97 74.64 69.78 70.23 69.80 C 80.77 59.67 81.41 41.33 71.45 30.60 C 63.60 21.32 49.75 18.52 38.65 23.16 C 31.27 18.80 21.83 18.68 14.27 22.69 C 20.65 14.79 29.32 8.56 39.30 6.22 Z' />\n<path fill='#90b4fe' d=' M 42.93 26.99 C 48.49 25.50 54.55 25.62 59.79 28.14 C 68.71 32.19 74.61 42.14 73.41 51.94 C 72.85 58.64 68.92 64.53 63.81 68.69 C 59.57 66.71 57.53 62.30 55.66 58.30 C 50.76 48.12 50.23 36.02 42.93 26.99 Z' />\n<path fill='#63b132' d=' M 12.30 33.30 C 17.11 28.49 24.33 26.90 30.91 28.06 C 25.22 33.49 21.44 41.03 21.46 48.99 C 21.11 58.97 26.58 68.76 35.08 73.92 C 43.28 79.06 53.95 79.28 62.66 75.29 C 70.37 77.57 78.52 77.36 86.31 75.57 C 80.05 84.00 70.94 90.35 60.69 92.84 C 48.02 96.03 34.00 93.24 23.56 85.37 C 12.16 77.09 5.12 63.11 5.44 49.00 C 5.15 43.06 8.22 37.42 12.30 33.30 Z' />\n<path fill='#91dc47' d=' M 26.94 54.00 C 24.97 45.06 29.20 35.59 36.45 30.24 C 41.99 33.71 44.23 40.14 46.55 45.91 C 43.00 53.40 38.44 60.46 35.94 68.42 C 31.50 64.74 27.96 59.77 26.94 54.00 Z' />\n<path fill='#91dc47' d=' M 41.97 71.80 C 41.46 64.27 45.31 57.52 48.11 50.80 C 50.40 58.13 51.84 66.19 57.18 72.06 C 52.17 73.37 46.93 73.26 41.97 71.80 Z' />\n</svg>";

//# sourceMappingURL=heads_up.js.map?rel=1439206059399
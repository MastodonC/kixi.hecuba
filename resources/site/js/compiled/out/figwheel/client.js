// Compiled by ClojureScript 0.0-3297 {}
goog.provide('figwheel.client');
goog.require('cljs.core');
goog.require('goog.Uri');
goog.require('cljs.core.async');
goog.require('figwheel.client.socket');
goog.require('figwheel.client.file_reloading');
goog.require('clojure.string');
goog.require('figwheel.client.utils');
goog.require('cljs.repl');
goog.require('figwheel.client.heads_up');
figwheel.client.figwheel_repl_print = (function figwheel$client$figwheel_repl_print(args){
figwheel.client.socket.send_BANG_.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"figwheel-event","figwheel-event",519570592),"callback",new cljs.core.Keyword(null,"callback-name","callback-name",336964714),"figwheel-repl-print",new cljs.core.Keyword(null,"content","content",15833224),args], null));

return args;
});
figwheel.client.console_print = (function figwheel$client$console_print(args){
console.log.apply(console,cljs.core.into_array.call(null,args));

return args;
});
figwheel.client.enable_repl_print_BANG_ = (function figwheel$client$enable_repl_print_BANG_(){
cljs.core._STAR_print_newline_STAR_ = false;

return cljs.core._STAR_print_fn_STAR_ = (function() { 
var G__41366__delegate = function (args){
return figwheel.client.figwheel_repl_print.call(null,figwheel.client.console_print.call(null,args));
};
var G__41366 = function (var_args){
var args = null;
if (arguments.length > 0) {
var G__41367__i = 0, G__41367__a = new Array(arguments.length -  0);
while (G__41367__i < G__41367__a.length) {G__41367__a[G__41367__i] = arguments[G__41367__i + 0]; ++G__41367__i;}
  args = new cljs.core.IndexedSeq(G__41367__a,0);
} 
return G__41366__delegate.call(this,args);};
G__41366.cljs$lang$maxFixedArity = 0;
G__41366.cljs$lang$applyTo = (function (arglist__41368){
var args = cljs.core.seq(arglist__41368);
return G__41366__delegate(args);
});
G__41366.cljs$core$IFn$_invoke$arity$variadic = G__41366__delegate;
return G__41366;
})()
;
});
figwheel.client.get_essential_messages = (function figwheel$client$get_essential_messages(ed){
if(cljs.core.truth_(ed)){
return cljs.core.cons.call(null,cljs.core.select_keys.call(null,ed,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"message","message",-406056002),new cljs.core.Keyword(null,"class","class",-2030961996)], null)),figwheel$client$get_essential_messages.call(null,new cljs.core.Keyword(null,"cause","cause",231901252).cljs$core$IFn$_invoke$arity$1(ed)));
} else {
return null;
}
});
figwheel.client.error_msg_format = (function figwheel$client$error_msg_format(p__41369){
var map__41371 = p__41369;
var map__41371__$1 = ((cljs.core.seq_QMARK_.call(null,map__41371))?cljs.core.apply.call(null,cljs.core.hash_map,map__41371):map__41371);
var message = cljs.core.get.call(null,map__41371__$1,new cljs.core.Keyword(null,"message","message",-406056002));
var class$ = cljs.core.get.call(null,map__41371__$1,new cljs.core.Keyword(null,"class","class",-2030961996));
return [cljs.core.str(class$),cljs.core.str(" : "),cljs.core.str(message)].join('');
});
figwheel.client.format_messages = cljs.core.comp.call(null,cljs.core.partial.call(null,cljs.core.map,figwheel.client.error_msg_format),figwheel.client.get_essential_messages);
figwheel.client.focus_msgs = (function figwheel$client$focus_msgs(name_set,msg_hist){
return cljs.core.cons.call(null,cljs.core.first.call(null,msg_hist),cljs.core.filter.call(null,cljs.core.comp.call(null,name_set,new cljs.core.Keyword(null,"msg-name","msg-name",-353709863)),cljs.core.rest.call(null,msg_hist)));
});
figwheel.client.reload_file_QMARK__STAR_ = (function figwheel$client$reload_file_QMARK__STAR_(msg_name,opts){
var or__16069__auto__ = new cljs.core.Keyword(null,"load-warninged-code","load-warninged-code",-2030345223).cljs$core$IFn$_invoke$arity$1(opts);
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return cljs.core.not_EQ_.call(null,msg_name,new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356));
}
});
figwheel.client.reload_file_state_QMARK_ = (function figwheel$client$reload_file_state_QMARK_(msg_names,opts){
var and__16057__auto__ = cljs.core._EQ_.call(null,cljs.core.first.call(null,msg_names),new cljs.core.Keyword(null,"files-changed","files-changed",-1418200563));
if(and__16057__auto__){
return figwheel.client.reload_file_QMARK__STAR_.call(null,cljs.core.second.call(null,msg_names),opts);
} else {
return and__16057__auto__;
}
});
figwheel.client.block_reload_file_state_QMARK_ = (function figwheel$client$block_reload_file_state_QMARK_(msg_names,opts){
return (cljs.core._EQ_.call(null,cljs.core.first.call(null,msg_names),new cljs.core.Keyword(null,"files-changed","files-changed",-1418200563))) && (cljs.core.not.call(null,figwheel.client.reload_file_QMARK__STAR_.call(null,cljs.core.second.call(null,msg_names),opts)));
});
figwheel.client.warning_append_state_QMARK_ = (function figwheel$client$warning_append_state_QMARK_(msg_names){
return cljs.core._EQ_.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356),new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356)], null),cljs.core.take.call(null,(2),msg_names));
});
figwheel.client.warning_state_QMARK_ = (function figwheel$client$warning_state_QMARK_(msg_names){
return cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356),cljs.core.first.call(null,msg_names));
});
figwheel.client.rewarning_state_QMARK_ = (function figwheel$client$rewarning_state_QMARK_(msg_names){
return cljs.core._EQ_.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356),new cljs.core.Keyword(null,"files-changed","files-changed",-1418200563),new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356)], null),cljs.core.take.call(null,(3),msg_names));
});
figwheel.client.compile_fail_state_QMARK_ = (function figwheel$client$compile_fail_state_QMARK_(msg_names){
return cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"compile-failed","compile-failed",-477639289),cljs.core.first.call(null,msg_names));
});
figwheel.client.compile_refail_state_QMARK_ = (function figwheel$client$compile_refail_state_QMARK_(msg_names){
return cljs.core._EQ_.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"compile-failed","compile-failed",-477639289),new cljs.core.Keyword(null,"compile-failed","compile-failed",-477639289)], null),cljs.core.take.call(null,(2),msg_names));
});
figwheel.client.css_loaded_state_QMARK_ = (function figwheel$client$css_loaded_state_QMARK_(msg_names){
return cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"css-files-changed","css-files-changed",720773874),cljs.core.first.call(null,msg_names));
});
figwheel.client.file_reloader_plugin = (function figwheel$client$file_reloader_plugin(opts){
var ch = cljs.core.async.chan.call(null);
var c__23633__auto___41500 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___41500,ch){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___41500,ch){
return (function (state_41474){
var state_val_41475 = (state_41474[(1)]);
if((state_val_41475 === (7))){
var inst_41470 = (state_41474[(2)]);
var state_41474__$1 = state_41474;
var statearr_41476_41501 = state_41474__$1;
(statearr_41476_41501[(2)] = inst_41470);

(statearr_41476_41501[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (1))){
var state_41474__$1 = state_41474;
var statearr_41477_41502 = state_41474__$1;
(statearr_41477_41502[(2)] = null);

(statearr_41477_41502[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (4))){
var inst_41438 = (state_41474[(7)]);
var inst_41438__$1 = (state_41474[(2)]);
var state_41474__$1 = (function (){var statearr_41478 = state_41474;
(statearr_41478[(7)] = inst_41438__$1);

return statearr_41478;
})();
if(cljs.core.truth_(inst_41438__$1)){
var statearr_41479_41503 = state_41474__$1;
(statearr_41479_41503[(1)] = (5));

} else {
var statearr_41480_41504 = state_41474__$1;
(statearr_41480_41504[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (13))){
var state_41474__$1 = state_41474;
var statearr_41481_41505 = state_41474__$1;
(statearr_41481_41505[(2)] = null);

(statearr_41481_41505[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (6))){
var state_41474__$1 = state_41474;
var statearr_41482_41506 = state_41474__$1;
(statearr_41482_41506[(2)] = null);

(statearr_41482_41506[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (3))){
var inst_41472 = (state_41474[(2)]);
var state_41474__$1 = state_41474;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_41474__$1,inst_41472);
} else {
if((state_val_41475 === (12))){
var inst_41445 = (state_41474[(8)]);
var inst_41458 = new cljs.core.Keyword(null,"files","files",-472457450).cljs$core$IFn$_invoke$arity$1(inst_41445);
var inst_41459 = cljs.core.first.call(null,inst_41458);
var inst_41460 = new cljs.core.Keyword(null,"file","file",-1269645878).cljs$core$IFn$_invoke$arity$1(inst_41459);
var inst_41461 = console.warn("Figwheel: Not loading code with warnings - ",inst_41460);
var state_41474__$1 = state_41474;
var statearr_41483_41507 = state_41474__$1;
(statearr_41483_41507[(2)] = inst_41461);

(statearr_41483_41507[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (2))){
var state_41474__$1 = state_41474;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41474__$1,(4),ch);
} else {
if((state_val_41475 === (11))){
var inst_41454 = (state_41474[(2)]);
var state_41474__$1 = state_41474;
var statearr_41484_41508 = state_41474__$1;
(statearr_41484_41508[(2)] = inst_41454);

(statearr_41484_41508[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (9))){
var inst_41444 = (state_41474[(9)]);
var inst_41456 = figwheel.client.block_reload_file_state_QMARK_.call(null,inst_41444,opts);
var state_41474__$1 = state_41474;
if(cljs.core.truth_(inst_41456)){
var statearr_41485_41509 = state_41474__$1;
(statearr_41485_41509[(1)] = (12));

} else {
var statearr_41486_41510 = state_41474__$1;
(statearr_41486_41510[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (5))){
var inst_41444 = (state_41474[(9)]);
var inst_41438 = (state_41474[(7)]);
var inst_41440 = [new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356),null,new cljs.core.Keyword(null,"files-changed","files-changed",-1418200563),null];
var inst_41441 = (new cljs.core.PersistentArrayMap(null,2,inst_41440,null));
var inst_41442 = (new cljs.core.PersistentHashSet(null,inst_41441,null));
var inst_41443 = figwheel.client.focus_msgs.call(null,inst_41442,inst_41438);
var inst_41444__$1 = cljs.core.map.call(null,new cljs.core.Keyword(null,"msg-name","msg-name",-353709863),inst_41443);
var inst_41445 = cljs.core.first.call(null,inst_41443);
var inst_41446 = figwheel.client.reload_file_state_QMARK_.call(null,inst_41444__$1,opts);
var state_41474__$1 = (function (){var statearr_41487 = state_41474;
(statearr_41487[(9)] = inst_41444__$1);

(statearr_41487[(8)] = inst_41445);

return statearr_41487;
})();
if(cljs.core.truth_(inst_41446)){
var statearr_41488_41511 = state_41474__$1;
(statearr_41488_41511[(1)] = (8));

} else {
var statearr_41489_41512 = state_41474__$1;
(statearr_41489_41512[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (14))){
var inst_41464 = (state_41474[(2)]);
var state_41474__$1 = state_41474;
var statearr_41490_41513 = state_41474__$1;
(statearr_41490_41513[(2)] = inst_41464);

(statearr_41490_41513[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (10))){
var inst_41466 = (state_41474[(2)]);
var state_41474__$1 = (function (){var statearr_41491 = state_41474;
(statearr_41491[(10)] = inst_41466);

return statearr_41491;
})();
var statearr_41492_41514 = state_41474__$1;
(statearr_41492_41514[(2)] = null);

(statearr_41492_41514[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41475 === (8))){
var inst_41445 = (state_41474[(8)]);
var inst_41448 = cljs.core.PersistentVector.EMPTY_NODE;
var inst_41449 = figwheel.client.file_reloading.reload_js_files.call(null,opts,inst_41445);
var inst_41450 = cljs.core.async.timeout.call(null,(1000));
var inst_41451 = [inst_41449,inst_41450];
var inst_41452 = (new cljs.core.PersistentVector(null,2,(5),inst_41448,inst_41451,null));
var state_41474__$1 = state_41474;
return cljs.core.async.ioc_alts_BANG_.call(null,state_41474__$1,(11),inst_41452);
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
}
}
}
});})(c__23633__auto___41500,ch))
;
return ((function (switch__23571__auto__,c__23633__auto___41500,ch){
return (function() {
var figwheel$client$file_reloader_plugin_$_state_machine__23572__auto__ = null;
var figwheel$client$file_reloader_plugin_$_state_machine__23572__auto____0 = (function (){
var statearr_41496 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_41496[(0)] = figwheel$client$file_reloader_plugin_$_state_machine__23572__auto__);

(statearr_41496[(1)] = (1));

return statearr_41496;
});
var figwheel$client$file_reloader_plugin_$_state_machine__23572__auto____1 = (function (state_41474){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_41474);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e41497){if((e41497 instanceof Object)){
var ex__23575__auto__ = e41497;
var statearr_41498_41515 = state_41474;
(statearr_41498_41515[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_41474);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e41497;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__41516 = state_41474;
state_41474 = G__41516;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$file_reloader_plugin_$_state_machine__23572__auto__ = function(state_41474){
switch(arguments.length){
case 0:
return figwheel$client$file_reloader_plugin_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$file_reloader_plugin_$_state_machine__23572__auto____1.call(this,state_41474);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$file_reloader_plugin_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$file_reloader_plugin_$_state_machine__23572__auto____0;
figwheel$client$file_reloader_plugin_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$file_reloader_plugin_$_state_machine__23572__auto____1;
return figwheel$client$file_reloader_plugin_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___41500,ch))
})();
var state__23635__auto__ = (function (){var statearr_41499 = f__23634__auto__.call(null);
(statearr_41499[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___41500);

return statearr_41499;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___41500,ch))
);


return ((function (ch){
return (function (msg_hist){
cljs.core.async.put_BANG_.call(null,ch,msg_hist);

return msg_hist;
});
;})(ch))
});
figwheel.client.truncate_stack_trace = (function figwheel$client$truncate_stack_trace(stack_str){
return cljs.core.take_while.call(null,(function (p1__41517_SHARP_){
return cljs.core.not.call(null,cljs.core.re_matches.call(null,/.*eval_javascript_STAR__STAR_.*/,p1__41517_SHARP_));
}),clojure.string.split_lines.call(null,stack_str));
});
var base_path_41524 = figwheel.client.utils.base_url_path.call(null);
figwheel.client.eval_javascript_STAR__STAR_ = ((function (base_path_41524){
return (function figwheel$client$eval_javascript_STAR__STAR_(code,opts,result_handler){
try{var _STAR_print_fn_STAR_41522 = cljs.core._STAR_print_fn_STAR_;
var _STAR_print_newline_STAR_41523 = cljs.core._STAR_print_newline_STAR_;
cljs.core._STAR_print_fn_STAR_ = ((function (_STAR_print_fn_STAR_41522,_STAR_print_newline_STAR_41523,base_path_41524){
return (function() { 
var G__41525__delegate = function (args){
return figwheel.client.figwheel_repl_print.call(null,figwheel.client.console_print.call(null,args));
};
var G__41525 = function (var_args){
var args = null;
if (arguments.length > 0) {
var G__41526__i = 0, G__41526__a = new Array(arguments.length -  0);
while (G__41526__i < G__41526__a.length) {G__41526__a[G__41526__i] = arguments[G__41526__i + 0]; ++G__41526__i;}
  args = new cljs.core.IndexedSeq(G__41526__a,0);
} 
return G__41525__delegate.call(this,args);};
G__41525.cljs$lang$maxFixedArity = 0;
G__41525.cljs$lang$applyTo = (function (arglist__41527){
var args = cljs.core.seq(arglist__41527);
return G__41525__delegate(args);
});
G__41525.cljs$core$IFn$_invoke$arity$variadic = G__41525__delegate;
return G__41525;
})()
;})(_STAR_print_fn_STAR_41522,_STAR_print_newline_STAR_41523,base_path_41524))
;

cljs.core._STAR_print_newline_STAR_ = false;

try{return result_handler.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"status","status",-1997798413),new cljs.core.Keyword(null,"success","success",1890645906),new cljs.core.Keyword(null,"value","value",305978217),[cljs.core.str(figwheel.client.utils.eval_helper.call(null,code,opts))].join('')], null));
}finally {cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR_41523;

cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR_41522;
}}catch (e41521){if((e41521 instanceof Error)){
var e = e41521;
return result_handler.call(null,new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"status","status",-1997798413),new cljs.core.Keyword(null,"exception","exception",-335277064),new cljs.core.Keyword(null,"value","value",305978217),cljs.core.pr_str.call(null,e),new cljs.core.Keyword(null,"stacktrace","stacktrace",-95588394),clojure.string.join.call(null,"\n",figwheel.client.truncate_stack_trace.call(null,e.stack)),new cljs.core.Keyword(null,"base-path","base-path",495760020),base_path_41524], null));
} else {
var e = e41521;
return result_handler.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"status","status",-1997798413),new cljs.core.Keyword(null,"exception","exception",-335277064),new cljs.core.Keyword(null,"value","value",305978217),cljs.core.pr_str.call(null,e),new cljs.core.Keyword(null,"stacktrace","stacktrace",-95588394),"No stacktrace available."], null));

}
}});})(base_path_41524))
;
/**
 * The REPL can disconnect and reconnect lets ensure cljs.user exists at least.
 */
figwheel.client.ensure_cljs_user = (function figwheel$client$ensure_cljs_user(){
if(cljs.core.truth_(cljs.user)){
return null;
} else {
return cljs.user = {};
}
});
figwheel.client.repl_plugin = (function figwheel$client$repl_plugin(p__41528){
var map__41533 = p__41528;
var map__41533__$1 = ((cljs.core.seq_QMARK_.call(null,map__41533))?cljs.core.apply.call(null,cljs.core.hash_map,map__41533):map__41533);
var opts = map__41533__$1;
var build_id = cljs.core.get.call(null,map__41533__$1,new cljs.core.Keyword(null,"build-id","build-id",1642831089));
return ((function (map__41533,map__41533__$1,opts,build_id){
return (function (p__41534){
var vec__41535 = p__41534;
var map__41536 = cljs.core.nth.call(null,vec__41535,(0),null);
var map__41536__$1 = ((cljs.core.seq_QMARK_.call(null,map__41536))?cljs.core.apply.call(null,cljs.core.hash_map,map__41536):map__41536);
var msg = map__41536__$1;
var msg_name = cljs.core.get.call(null,map__41536__$1,new cljs.core.Keyword(null,"msg-name","msg-name",-353709863));
var _ = cljs.core.nthnext.call(null,vec__41535,(1));
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"repl-eval","repl-eval",-1784727398),msg_name)){
figwheel.client.ensure_cljs_user.call(null);

return figwheel.client.eval_javascript_STAR__STAR_.call(null,new cljs.core.Keyword(null,"code","code",1586293142).cljs$core$IFn$_invoke$arity$1(msg),opts,((function (vec__41535,map__41536,map__41536__$1,msg,msg_name,_,map__41533,map__41533__$1,opts,build_id){
return (function (res){
return figwheel.client.socket.send_BANG_.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"figwheel-event","figwheel-event",519570592),"callback",new cljs.core.Keyword(null,"callback-name","callback-name",336964714),new cljs.core.Keyword(null,"callback-name","callback-name",336964714).cljs$core$IFn$_invoke$arity$1(msg),new cljs.core.Keyword(null,"content","content",15833224),res], null));
});})(vec__41535,map__41536,map__41536__$1,msg,msg_name,_,map__41533,map__41533__$1,opts,build_id))
);
} else {
return null;
}
});
;})(map__41533,map__41533__$1,opts,build_id))
});
figwheel.client.css_reloader_plugin = (function figwheel$client$css_reloader_plugin(opts){
return (function (p__41540){
var vec__41541 = p__41540;
var map__41542 = cljs.core.nth.call(null,vec__41541,(0),null);
var map__41542__$1 = ((cljs.core.seq_QMARK_.call(null,map__41542))?cljs.core.apply.call(null,cljs.core.hash_map,map__41542):map__41542);
var msg = map__41542__$1;
var msg_name = cljs.core.get.call(null,map__41542__$1,new cljs.core.Keyword(null,"msg-name","msg-name",-353709863));
var _ = cljs.core.nthnext.call(null,vec__41541,(1));
if(cljs.core._EQ_.call(null,msg_name,new cljs.core.Keyword(null,"css-files-changed","css-files-changed",720773874))){
return figwheel.client.file_reloading.reload_css_files.call(null,opts,msg);
} else {
return null;
}
});
});
figwheel.client.compile_fail_warning_plugin = (function figwheel$client$compile_fail_warning_plugin(p__41543){
var map__41551 = p__41543;
var map__41551__$1 = ((cljs.core.seq_QMARK_.call(null,map__41551))?cljs.core.apply.call(null,cljs.core.hash_map,map__41551):map__41551);
var on_compile_warning = cljs.core.get.call(null,map__41551__$1,new cljs.core.Keyword(null,"on-compile-warning","on-compile-warning",-1195585947));
var on_compile_fail = cljs.core.get.call(null,map__41551__$1,new cljs.core.Keyword(null,"on-compile-fail","on-compile-fail",728013036));
return ((function (map__41551,map__41551__$1,on_compile_warning,on_compile_fail){
return (function (p__41552){
var vec__41553 = p__41552;
var map__41554 = cljs.core.nth.call(null,vec__41553,(0),null);
var map__41554__$1 = ((cljs.core.seq_QMARK_.call(null,map__41554))?cljs.core.apply.call(null,cljs.core.hash_map,map__41554):map__41554);
var msg = map__41554__$1;
var msg_name = cljs.core.get.call(null,map__41554__$1,new cljs.core.Keyword(null,"msg-name","msg-name",-353709863));
var _ = cljs.core.nthnext.call(null,vec__41553,(1));
var pred__41555 = cljs.core._EQ_;
var expr__41556 = msg_name;
if(cljs.core.truth_(pred__41555.call(null,new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356),expr__41556))){
return on_compile_warning.call(null,msg);
} else {
if(cljs.core.truth_(pred__41555.call(null,new cljs.core.Keyword(null,"compile-failed","compile-failed",-477639289),expr__41556))){
return on_compile_fail.call(null,msg);
} else {
return null;
}
}
});
;})(map__41551,map__41551__$1,on_compile_warning,on_compile_fail))
});
figwheel.client.heads_up_plugin_msg_handler = (function figwheel$client$heads_up_plugin_msg_handler(opts,msg_hist_SINGLEQUOTE_){
var msg_hist = figwheel.client.focus_msgs.call(null,new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"compile-failed","compile-failed",-477639289),null,new cljs.core.Keyword(null,"compile-warning","compile-warning",43425356),null,new cljs.core.Keyword(null,"files-changed","files-changed",-1418200563),null], null), null),msg_hist_SINGLEQUOTE_);
var msg_names = cljs.core.map.call(null,new cljs.core.Keyword(null,"msg-name","msg-name",-353709863),msg_hist);
var msg = cljs.core.first.call(null,msg_hist);
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__,msg_hist,msg_names,msg){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__,msg_hist,msg_names,msg){
return (function (state_41757){
var state_val_41758 = (state_41757[(1)]);
if((state_val_41758 === (7))){
var inst_41691 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41759_41800 = state_41757__$1;
(statearr_41759_41800[(2)] = inst_41691);

(statearr_41759_41800[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (20))){
var inst_41719 = figwheel.client.rewarning_state_QMARK_.call(null,msg_names);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41719)){
var statearr_41760_41801 = state_41757__$1;
(statearr_41760_41801[(1)] = (22));

} else {
var statearr_41761_41802 = state_41757__$1;
(statearr_41761_41802[(1)] = (23));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (27))){
var inst_41731 = new cljs.core.Keyword(null,"message","message",-406056002).cljs$core$IFn$_invoke$arity$1(msg);
var inst_41732 = figwheel.client.heads_up.display_warning.call(null,inst_41731);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(30),inst_41732);
} else {
if((state_val_41758 === (1))){
var inst_41679 = figwheel.client.reload_file_state_QMARK_.call(null,msg_names,opts);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41679)){
var statearr_41762_41803 = state_41757__$1;
(statearr_41762_41803[(1)] = (2));

} else {
var statearr_41763_41804 = state_41757__$1;
(statearr_41763_41804[(1)] = (3));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (24))){
var inst_41747 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41764_41805 = state_41757__$1;
(statearr_41764_41805[(2)] = inst_41747);

(statearr_41764_41805[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (4))){
var inst_41755 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_41757__$1,inst_41755);
} else {
if((state_val_41758 === (15))){
var inst_41707 = new cljs.core.Keyword(null,"exception-data","exception-data",-512474886).cljs$core$IFn$_invoke$arity$1(msg);
var inst_41708 = figwheel.client.format_messages.call(null,inst_41707);
var inst_41709 = new cljs.core.Keyword(null,"cause","cause",231901252).cljs$core$IFn$_invoke$arity$1(msg);
var inst_41710 = figwheel.client.heads_up.display_error.call(null,inst_41708,inst_41709);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(18),inst_41710);
} else {
if((state_val_41758 === (21))){
var inst_41749 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41765_41806 = state_41757__$1;
(statearr_41765_41806[(2)] = inst_41749);

(statearr_41765_41806[(1)] = (17));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (31))){
var inst_41738 = figwheel.client.heads_up.flash_loaded.call(null);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(34),inst_41738);
} else {
if((state_val_41758 === (32))){
var state_41757__$1 = state_41757;
var statearr_41766_41807 = state_41757__$1;
(statearr_41766_41807[(2)] = null);

(statearr_41766_41807[(1)] = (33));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (33))){
var inst_41743 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41767_41808 = state_41757__$1;
(statearr_41767_41808[(2)] = inst_41743);

(statearr_41767_41808[(1)] = (29));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (13))){
var inst_41697 = (state_41757[(2)]);
var inst_41698 = new cljs.core.Keyword(null,"exception-data","exception-data",-512474886).cljs$core$IFn$_invoke$arity$1(msg);
var inst_41699 = figwheel.client.format_messages.call(null,inst_41698);
var inst_41700 = new cljs.core.Keyword(null,"cause","cause",231901252).cljs$core$IFn$_invoke$arity$1(msg);
var inst_41701 = figwheel.client.heads_up.display_error.call(null,inst_41699,inst_41700);
var state_41757__$1 = (function (){var statearr_41768 = state_41757;
(statearr_41768[(7)] = inst_41697);

return statearr_41768;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(14),inst_41701);
} else {
if((state_val_41758 === (22))){
var inst_41721 = figwheel.client.heads_up.clear.call(null);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(25),inst_41721);
} else {
if((state_val_41758 === (29))){
var inst_41745 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41769_41809 = state_41757__$1;
(statearr_41769_41809[(2)] = inst_41745);

(statearr_41769_41809[(1)] = (24));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (6))){
var inst_41687 = figwheel.client.heads_up.clear.call(null);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(9),inst_41687);
} else {
if((state_val_41758 === (28))){
var inst_41736 = figwheel.client.css_loaded_state_QMARK_.call(null,msg_names);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41736)){
var statearr_41770_41810 = state_41757__$1;
(statearr_41770_41810[(1)] = (31));

} else {
var statearr_41771_41811 = state_41757__$1;
(statearr_41771_41811[(1)] = (32));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (25))){
var inst_41723 = (state_41757[(2)]);
var inst_41724 = new cljs.core.Keyword(null,"message","message",-406056002).cljs$core$IFn$_invoke$arity$1(msg);
var inst_41725 = figwheel.client.heads_up.display_warning.call(null,inst_41724);
var state_41757__$1 = (function (){var statearr_41772 = state_41757;
(statearr_41772[(8)] = inst_41723);

return statearr_41772;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(26),inst_41725);
} else {
if((state_val_41758 === (34))){
var inst_41740 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41773_41812 = state_41757__$1;
(statearr_41773_41812[(2)] = inst_41740);

(statearr_41773_41812[(1)] = (33));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (17))){
var inst_41751 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41774_41813 = state_41757__$1;
(statearr_41774_41813[(2)] = inst_41751);

(statearr_41774_41813[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (3))){
var inst_41693 = figwheel.client.compile_refail_state_QMARK_.call(null,msg_names);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41693)){
var statearr_41775_41814 = state_41757__$1;
(statearr_41775_41814[(1)] = (10));

} else {
var statearr_41776_41815 = state_41757__$1;
(statearr_41776_41815[(1)] = (11));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (12))){
var inst_41753 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41777_41816 = state_41757__$1;
(statearr_41777_41816[(2)] = inst_41753);

(statearr_41777_41816[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (2))){
var inst_41681 = new cljs.core.Keyword(null,"autoload","autoload",-354122500).cljs$core$IFn$_invoke$arity$1(opts);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41681)){
var statearr_41778_41817 = state_41757__$1;
(statearr_41778_41817[(1)] = (5));

} else {
var statearr_41779_41818 = state_41757__$1;
(statearr_41779_41818[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (23))){
var inst_41729 = figwheel.client.warning_state_QMARK_.call(null,msg_names);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41729)){
var statearr_41780_41819 = state_41757__$1;
(statearr_41780_41819[(1)] = (27));

} else {
var statearr_41781_41820 = state_41757__$1;
(statearr_41781_41820[(1)] = (28));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (19))){
var inst_41716 = new cljs.core.Keyword(null,"message","message",-406056002).cljs$core$IFn$_invoke$arity$1(msg);
var inst_41717 = figwheel.client.heads_up.append_message.call(null,inst_41716);
var state_41757__$1 = state_41757;
var statearr_41782_41821 = state_41757__$1;
(statearr_41782_41821[(2)] = inst_41717);

(statearr_41782_41821[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (11))){
var inst_41705 = figwheel.client.compile_fail_state_QMARK_.call(null,msg_names);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41705)){
var statearr_41783_41822 = state_41757__$1;
(statearr_41783_41822[(1)] = (15));

} else {
var statearr_41784_41823 = state_41757__$1;
(statearr_41784_41823[(1)] = (16));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (9))){
var inst_41689 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41785_41824 = state_41757__$1;
(statearr_41785_41824[(2)] = inst_41689);

(statearr_41785_41824[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (5))){
var inst_41683 = figwheel.client.heads_up.flash_loaded.call(null);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(8),inst_41683);
} else {
if((state_val_41758 === (14))){
var inst_41703 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41786_41825 = state_41757__$1;
(statearr_41786_41825[(2)] = inst_41703);

(statearr_41786_41825[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (26))){
var inst_41727 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41787_41826 = state_41757__$1;
(statearr_41787_41826[(2)] = inst_41727);

(statearr_41787_41826[(1)] = (24));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (16))){
var inst_41714 = figwheel.client.warning_append_state_QMARK_.call(null,msg_names);
var state_41757__$1 = state_41757;
if(cljs.core.truth_(inst_41714)){
var statearr_41788_41827 = state_41757__$1;
(statearr_41788_41827[(1)] = (19));

} else {
var statearr_41789_41828 = state_41757__$1;
(statearr_41789_41828[(1)] = (20));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (30))){
var inst_41734 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41790_41829 = state_41757__$1;
(statearr_41790_41829[(2)] = inst_41734);

(statearr_41790_41829[(1)] = (29));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (10))){
var inst_41695 = figwheel.client.heads_up.clear.call(null);
var state_41757__$1 = state_41757;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41757__$1,(13),inst_41695);
} else {
if((state_val_41758 === (18))){
var inst_41712 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41791_41830 = state_41757__$1;
(statearr_41791_41830[(2)] = inst_41712);

(statearr_41791_41830[(1)] = (17));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41758 === (8))){
var inst_41685 = (state_41757[(2)]);
var state_41757__$1 = state_41757;
var statearr_41792_41831 = state_41757__$1;
(statearr_41792_41831[(2)] = inst_41685);

(statearr_41792_41831[(1)] = (7));


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
}
});})(c__23633__auto__,msg_hist,msg_names,msg))
;
return ((function (switch__23571__auto__,c__23633__auto__,msg_hist,msg_names,msg){
return (function() {
var figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto__ = null;
var figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto____0 = (function (){
var statearr_41796 = [null,null,null,null,null,null,null,null,null];
(statearr_41796[(0)] = figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto__);

(statearr_41796[(1)] = (1));

return statearr_41796;
});
var figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto____1 = (function (state_41757){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_41757);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e41797){if((e41797 instanceof Object)){
var ex__23575__auto__ = e41797;
var statearr_41798_41832 = state_41757;
(statearr_41798_41832[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_41757);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e41797;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__41833 = state_41757;
state_41757 = G__41833;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto__ = function(state_41757){
switch(arguments.length){
case 0:
return figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto____1.call(this,state_41757);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto____0;
figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto____1;
return figwheel$client$heads_up_plugin_msg_handler_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__,msg_hist,msg_names,msg))
})();
var state__23635__auto__ = (function (){var statearr_41799 = f__23634__auto__.call(null);
(statearr_41799[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_41799;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__,msg_hist,msg_names,msg))
);

return c__23633__auto__;
});
figwheel.client.heads_up_plugin = (function figwheel$client$heads_up_plugin(opts){
var ch = cljs.core.async.chan.call(null);
figwheel.client.heads_up_config_options_STAR__STAR_ = opts;

var c__23633__auto___41896 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___41896,ch){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___41896,ch){
return (function (state_41879){
var state_val_41880 = (state_41879[(1)]);
if((state_val_41880 === (1))){
var state_41879__$1 = state_41879;
var statearr_41881_41897 = state_41879__$1;
(statearr_41881_41897[(2)] = null);

(statearr_41881_41897[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41880 === (2))){
var state_41879__$1 = state_41879;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41879__$1,(4),ch);
} else {
if((state_val_41880 === (3))){
var inst_41877 = (state_41879[(2)]);
var state_41879__$1 = state_41879;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_41879__$1,inst_41877);
} else {
if((state_val_41880 === (4))){
var inst_41867 = (state_41879[(7)]);
var inst_41867__$1 = (state_41879[(2)]);
var state_41879__$1 = (function (){var statearr_41882 = state_41879;
(statearr_41882[(7)] = inst_41867__$1);

return statearr_41882;
})();
if(cljs.core.truth_(inst_41867__$1)){
var statearr_41883_41898 = state_41879__$1;
(statearr_41883_41898[(1)] = (5));

} else {
var statearr_41884_41899 = state_41879__$1;
(statearr_41884_41899[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41880 === (5))){
var inst_41867 = (state_41879[(7)]);
var inst_41869 = figwheel.client.heads_up_plugin_msg_handler.call(null,opts,inst_41867);
var state_41879__$1 = state_41879;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41879__$1,(8),inst_41869);
} else {
if((state_val_41880 === (6))){
var state_41879__$1 = state_41879;
var statearr_41885_41900 = state_41879__$1;
(statearr_41885_41900[(2)] = null);

(statearr_41885_41900[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41880 === (7))){
var inst_41875 = (state_41879[(2)]);
var state_41879__$1 = state_41879;
var statearr_41886_41901 = state_41879__$1;
(statearr_41886_41901[(2)] = inst_41875);

(statearr_41886_41901[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_41880 === (8))){
var inst_41871 = (state_41879[(2)]);
var state_41879__$1 = (function (){var statearr_41887 = state_41879;
(statearr_41887[(8)] = inst_41871);

return statearr_41887;
})();
var statearr_41888_41902 = state_41879__$1;
(statearr_41888_41902[(2)] = null);

(statearr_41888_41902[(1)] = (2));


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
});})(c__23633__auto___41896,ch))
;
return ((function (switch__23571__auto__,c__23633__auto___41896,ch){
return (function() {
var figwheel$client$heads_up_plugin_$_state_machine__23572__auto__ = null;
var figwheel$client$heads_up_plugin_$_state_machine__23572__auto____0 = (function (){
var statearr_41892 = [null,null,null,null,null,null,null,null,null];
(statearr_41892[(0)] = figwheel$client$heads_up_plugin_$_state_machine__23572__auto__);

(statearr_41892[(1)] = (1));

return statearr_41892;
});
var figwheel$client$heads_up_plugin_$_state_machine__23572__auto____1 = (function (state_41879){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_41879);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e41893){if((e41893 instanceof Object)){
var ex__23575__auto__ = e41893;
var statearr_41894_41903 = state_41879;
(statearr_41894_41903[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_41879);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e41893;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__41904 = state_41879;
state_41879 = G__41904;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$heads_up_plugin_$_state_machine__23572__auto__ = function(state_41879){
switch(arguments.length){
case 0:
return figwheel$client$heads_up_plugin_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$heads_up_plugin_$_state_machine__23572__auto____1.call(this,state_41879);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$heads_up_plugin_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$heads_up_plugin_$_state_machine__23572__auto____0;
figwheel$client$heads_up_plugin_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$heads_up_plugin_$_state_machine__23572__auto____1;
return figwheel$client$heads_up_plugin_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___41896,ch))
})();
var state__23635__auto__ = (function (){var statearr_41895 = f__23634__auto__.call(null);
(statearr_41895[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___41896);

return statearr_41895;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___41896,ch))
);


figwheel.client.heads_up.ensure_container.call(null);

return ((function (ch){
return (function (msg_hist){
cljs.core.async.put_BANG_.call(null,ch,msg_hist);

return msg_hist;
});
;})(ch))
});
figwheel.client.enforce_project_plugin = (function figwheel$client$enforce_project_plugin(opts){
return (function (msg_hist){
if(((1) < cljs.core.count.call(null,cljs.core.set.call(null,cljs.core.keep.call(null,new cljs.core.Keyword(null,"project-id","project-id",206449307),cljs.core.take.call(null,(5),msg_hist)))))){
figwheel.client.socket.close_BANG_.call(null);

console.error("Figwheel: message received from different project. Shutting socket down.");

if(cljs.core.truth_(new cljs.core.Keyword(null,"heads-up-display","heads-up-display",-896577202).cljs$core$IFn$_invoke$arity$1(opts))){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_41925){
var state_val_41926 = (state_41925[(1)]);
if((state_val_41926 === (1))){
var inst_41920 = cljs.core.async.timeout.call(null,(3000));
var state_41925__$1 = state_41925;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_41925__$1,(2),inst_41920);
} else {
if((state_val_41926 === (2))){
var inst_41922 = (state_41925[(2)]);
var inst_41923 = figwheel.client.heads_up.display_system_warning.call(null,"Connection from different project","Shutting connection down!!!!!");
var state_41925__$1 = (function (){var statearr_41927 = state_41925;
(statearr_41927[(7)] = inst_41922);

return statearr_41927;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_41925__$1,inst_41923);
} else {
return null;
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var figwheel$client$enforce_project_plugin_$_state_machine__23572__auto__ = null;
var figwheel$client$enforce_project_plugin_$_state_machine__23572__auto____0 = (function (){
var statearr_41931 = [null,null,null,null,null,null,null,null];
(statearr_41931[(0)] = figwheel$client$enforce_project_plugin_$_state_machine__23572__auto__);

(statearr_41931[(1)] = (1));

return statearr_41931;
});
var figwheel$client$enforce_project_plugin_$_state_machine__23572__auto____1 = (function (state_41925){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_41925);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e41932){if((e41932 instanceof Object)){
var ex__23575__auto__ = e41932;
var statearr_41933_41935 = state_41925;
(statearr_41933_41935[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_41925);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e41932;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__41936 = state_41925;
state_41925 = G__41936;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$enforce_project_plugin_$_state_machine__23572__auto__ = function(state_41925){
switch(arguments.length){
case 0:
return figwheel$client$enforce_project_plugin_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$enforce_project_plugin_$_state_machine__23572__auto____1.call(this,state_41925);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$enforce_project_plugin_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$enforce_project_plugin_$_state_machine__23572__auto____0;
figwheel$client$enforce_project_plugin_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$enforce_project_plugin_$_state_machine__23572__auto____1;
return figwheel$client$enforce_project_plugin_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_41934 = f__23634__auto__.call(null);
(statearr_41934[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_41934;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
} else {
return null;
}
} else {
return null;
}
});
});
figwheel.client.default_on_jsload = cljs.core.identity;
figwheel.client.default_on_compile_fail = (function figwheel$client$default_on_compile_fail(p__41937){
var map__41943 = p__41937;
var map__41943__$1 = ((cljs.core.seq_QMARK_.call(null,map__41943))?cljs.core.apply.call(null,cljs.core.hash_map,map__41943):map__41943);
var ed = map__41943__$1;
var formatted_exception = cljs.core.get.call(null,map__41943__$1,new cljs.core.Keyword(null,"formatted-exception","formatted-exception",-116489026));
var exception_data = cljs.core.get.call(null,map__41943__$1,new cljs.core.Keyword(null,"exception-data","exception-data",-512474886));
var cause = cljs.core.get.call(null,map__41943__$1,new cljs.core.Keyword(null,"cause","cause",231901252));
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"debug","debug",-1608172596),"Figwheel: Compile Exception");

var seq__41944_41948 = cljs.core.seq.call(null,figwheel.client.format_messages.call(null,exception_data));
var chunk__41945_41949 = null;
var count__41946_41950 = (0);
var i__41947_41951 = (0);
while(true){
if((i__41947_41951 < count__41946_41950)){
var msg_41952 = cljs.core._nth.call(null,chunk__41945_41949,i__41947_41951);
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"info","info",-317069002),msg_41952);

var G__41953 = seq__41944_41948;
var G__41954 = chunk__41945_41949;
var G__41955 = count__41946_41950;
var G__41956 = (i__41947_41951 + (1));
seq__41944_41948 = G__41953;
chunk__41945_41949 = G__41954;
count__41946_41950 = G__41955;
i__41947_41951 = G__41956;
continue;
} else {
var temp__4425__auto___41957 = cljs.core.seq.call(null,seq__41944_41948);
if(temp__4425__auto___41957){
var seq__41944_41958__$1 = temp__4425__auto___41957;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__41944_41958__$1)){
var c__16854__auto___41959 = cljs.core.chunk_first.call(null,seq__41944_41958__$1);
var G__41960 = cljs.core.chunk_rest.call(null,seq__41944_41958__$1);
var G__41961 = c__16854__auto___41959;
var G__41962 = cljs.core.count.call(null,c__16854__auto___41959);
var G__41963 = (0);
seq__41944_41948 = G__41960;
chunk__41945_41949 = G__41961;
count__41946_41950 = G__41962;
i__41947_41951 = G__41963;
continue;
} else {
var msg_41964 = cljs.core.first.call(null,seq__41944_41958__$1);
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"info","info",-317069002),msg_41964);

var G__41965 = cljs.core.next.call(null,seq__41944_41958__$1);
var G__41966 = null;
var G__41967 = (0);
var G__41968 = (0);
seq__41944_41948 = G__41965;
chunk__41945_41949 = G__41966;
count__41946_41950 = G__41967;
i__41947_41951 = G__41968;
continue;
}
} else {
}
}
break;
}

if(cljs.core.truth_(cause)){
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"info","info",-317069002),[cljs.core.str("Error on file "),cljs.core.str(new cljs.core.Keyword(null,"file","file",-1269645878).cljs$core$IFn$_invoke$arity$1(cause)),cljs.core.str(", line "),cljs.core.str(new cljs.core.Keyword(null,"line","line",212345235).cljs$core$IFn$_invoke$arity$1(cause)),cljs.core.str(", column "),cljs.core.str(new cljs.core.Keyword(null,"column","column",2078222095).cljs$core$IFn$_invoke$arity$1(cause))].join(''));
} else {
}

return ed;
});
figwheel.client.default_on_compile_warning = (function figwheel$client$default_on_compile_warning(p__41969){
var map__41971 = p__41969;
var map__41971__$1 = ((cljs.core.seq_QMARK_.call(null,map__41971))?cljs.core.apply.call(null,cljs.core.hash_map,map__41971):map__41971);
var w = map__41971__$1;
var message = cljs.core.get.call(null,map__41971__$1,new cljs.core.Keyword(null,"message","message",-406056002));
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"warn","warn",-436710552),[cljs.core.str("Figwheel: Compile Warning - "),cljs.core.str(message)].join(''));

return w;
});
figwheel.client.default_before_load = (function figwheel$client$default_before_load(files){
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"debug","debug",-1608172596),"Figwheel: notified of file changes");

return files;
});
figwheel.client.default_on_cssload = (function figwheel$client$default_on_cssload(files){
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"debug","debug",-1608172596),"Figwheel: loaded CSS files");

figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"info","info",-317069002),cljs.core.pr_str.call(null,cljs.core.map.call(null,new cljs.core.Keyword(null,"file","file",-1269645878),files)));

return files;
});
if(typeof figwheel.client.config_defaults !== 'undefined'){
} else {
figwheel.client.config_defaults = cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"load-unchanged-files","load-unchanged-files",-1561468704),new cljs.core.Keyword(null,"on-compile-warning","on-compile-warning",-1195585947),new cljs.core.Keyword(null,"on-jsload","on-jsload",-395756602),new cljs.core.Keyword(null,"on-compile-fail","on-compile-fail",728013036),new cljs.core.Keyword(null,"debug","debug",-1608172596),new cljs.core.Keyword(null,"heads-up-display","heads-up-display",-896577202),new cljs.core.Keyword(null,"websocket-url","websocket-url",-490444938),new cljs.core.Keyword(null,"before-jsload","before-jsload",-847513128),new cljs.core.Keyword(null,"load-warninged-code","load-warninged-code",-2030345223),new cljs.core.Keyword(null,"eval-fn","eval-fn",-1111644294),new cljs.core.Keyword(null,"retry-count","retry-count",1936122875),new cljs.core.Keyword(null,"autoload","autoload",-354122500),new cljs.core.Keyword(null,"url-rewriter","url-rewriter",200543838),new cljs.core.Keyword(null,"on-cssload","on-cssload",1825432318)],[true,figwheel.client.default_on_compile_warning,figwheel.client.default_on_jsload,figwheel.client.default_on_compile_fail,false,true,[cljs.core.str("ws://"),cljs.core.str((cljs.core.truth_(figwheel.client.utils.html_env_QMARK_.call(null))?location.host:"localhost:3449")),cljs.core.str("/figwheel-ws")].join(''),figwheel.client.default_before_load,false,false,(100),true,false,figwheel.client.default_on_cssload]);
}
figwheel.client.handle_deprecated_jsload_callback = (function figwheel$client$handle_deprecated_jsload_callback(config){
if(cljs.core.truth_(new cljs.core.Keyword(null,"jsload-callback","jsload-callback",-1949628369).cljs$core$IFn$_invoke$arity$1(config))){
return cljs.core.dissoc.call(null,cljs.core.assoc.call(null,config,new cljs.core.Keyword(null,"on-jsload","on-jsload",-395756602),new cljs.core.Keyword(null,"jsload-callback","jsload-callback",-1949628369).cljs$core$IFn$_invoke$arity$1(config)),new cljs.core.Keyword(null,"jsload-callback","jsload-callback",-1949628369));
} else {
return config;
}
});
figwheel.client.base_plugins = (function figwheel$client$base_plugins(system_options){
var base = new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"enforce-project-plugin","enforce-project-plugin",959402899),figwheel.client.enforce_project_plugin,new cljs.core.Keyword(null,"file-reloader-plugin","file-reloader-plugin",-1792964733),figwheel.client.file_reloader_plugin,new cljs.core.Keyword(null,"comp-fail-warning-plugin","comp-fail-warning-plugin",634311),figwheel.client.compile_fail_warning_plugin,new cljs.core.Keyword(null,"css-reloader-plugin","css-reloader-plugin",2002032904),figwheel.client.css_reloader_plugin,new cljs.core.Keyword(null,"repl-plugin","repl-plugin",-1138952371),figwheel.client.repl_plugin], null);
var base__$1 = ((cljs.core.not.call(null,figwheel.client.utils.html_env_QMARK_.call(null)))?cljs.core.select_keys.call(null,base,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"file-reloader-plugin","file-reloader-plugin",-1792964733),new cljs.core.Keyword(null,"comp-fail-warning-plugin","comp-fail-warning-plugin",634311),new cljs.core.Keyword(null,"repl-plugin","repl-plugin",-1138952371)], null)):base);
var base__$2 = ((new cljs.core.Keyword(null,"autoload","autoload",-354122500).cljs$core$IFn$_invoke$arity$1(system_options) === false)?cljs.core.dissoc.call(null,base__$1,new cljs.core.Keyword(null,"file-reloader-plugin","file-reloader-plugin",-1792964733)):base__$1);
if(cljs.core.truth_((function (){var and__16057__auto__ = new cljs.core.Keyword(null,"heads-up-display","heads-up-display",-896577202).cljs$core$IFn$_invoke$arity$1(system_options);
if(cljs.core.truth_(and__16057__auto__)){
return figwheel.client.utils.html_env_QMARK_.call(null);
} else {
return and__16057__auto__;
}
})())){
return cljs.core.assoc.call(null,base__$2,new cljs.core.Keyword(null,"heads-up-display-plugin","heads-up-display-plugin",1745207501),figwheel.client.heads_up_plugin);
} else {
return base__$2;
}
});
figwheel.client.add_plugins = (function figwheel$client$add_plugins(plugins,system_options){
var seq__41978 = cljs.core.seq.call(null,plugins);
var chunk__41979 = null;
var count__41980 = (0);
var i__41981 = (0);
while(true){
if((i__41981 < count__41980)){
var vec__41982 = cljs.core._nth.call(null,chunk__41979,i__41981);
var k = cljs.core.nth.call(null,vec__41982,(0),null);
var plugin = cljs.core.nth.call(null,vec__41982,(1),null);
if(cljs.core.truth_(plugin)){
var pl_41984 = plugin.call(null,system_options);
cljs.core.add_watch.call(null,figwheel.client.socket.message_history_atom,k,((function (seq__41978,chunk__41979,count__41980,i__41981,pl_41984,vec__41982,k,plugin){
return (function (_,___$1,___$2,msg_hist){
return pl_41984.call(null,msg_hist);
});})(seq__41978,chunk__41979,count__41980,i__41981,pl_41984,vec__41982,k,plugin))
);
} else {
}

var G__41985 = seq__41978;
var G__41986 = chunk__41979;
var G__41987 = count__41980;
var G__41988 = (i__41981 + (1));
seq__41978 = G__41985;
chunk__41979 = G__41986;
count__41980 = G__41987;
i__41981 = G__41988;
continue;
} else {
var temp__4425__auto__ = cljs.core.seq.call(null,seq__41978);
if(temp__4425__auto__){
var seq__41978__$1 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__41978__$1)){
var c__16854__auto__ = cljs.core.chunk_first.call(null,seq__41978__$1);
var G__41989 = cljs.core.chunk_rest.call(null,seq__41978__$1);
var G__41990 = c__16854__auto__;
var G__41991 = cljs.core.count.call(null,c__16854__auto__);
var G__41992 = (0);
seq__41978 = G__41989;
chunk__41979 = G__41990;
count__41980 = G__41991;
i__41981 = G__41992;
continue;
} else {
var vec__41983 = cljs.core.first.call(null,seq__41978__$1);
var k = cljs.core.nth.call(null,vec__41983,(0),null);
var plugin = cljs.core.nth.call(null,vec__41983,(1),null);
if(cljs.core.truth_(plugin)){
var pl_41993 = plugin.call(null,system_options);
cljs.core.add_watch.call(null,figwheel.client.socket.message_history_atom,k,((function (seq__41978,chunk__41979,count__41980,i__41981,pl_41993,vec__41983,k,plugin,seq__41978__$1,temp__4425__auto__){
return (function (_,___$1,___$2,msg_hist){
return pl_41993.call(null,msg_hist);
});})(seq__41978,chunk__41979,count__41980,i__41981,pl_41993,vec__41983,k,plugin,seq__41978__$1,temp__4425__auto__))
);
} else {
}

var G__41994 = cljs.core.next.call(null,seq__41978__$1);
var G__41995 = null;
var G__41996 = (0);
var G__41997 = (0);
seq__41978 = G__41994;
chunk__41979 = G__41995;
count__41980 = G__41996;
i__41981 = G__41997;
continue;
}
} else {
return null;
}
}
break;
}
});
figwheel.client.start = (function figwheel$client$start(){
var G__41999 = arguments.length;
switch (G__41999) {
case 1:
return figwheel.client.start.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 0:
return figwheel.client.start.cljs$core$IFn$_invoke$arity$0();

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

figwheel.client.start.cljs$core$IFn$_invoke$arity$1 = (function (opts){
if((goog.dependencies_ == null)){
return null;
} else {
if(typeof figwheel.client.__figwheel_start_once__ !== 'undefined'){
return null;
} else {
figwheel.client.__figwheel_start_once__ = setTimeout((function (){
var plugins_SINGLEQUOTE_ = new cljs.core.Keyword(null,"plugins","plugins",1900073717).cljs$core$IFn$_invoke$arity$1(opts);
var merge_plugins = new cljs.core.Keyword(null,"merge-plugins","merge-plugins",-1193912370).cljs$core$IFn$_invoke$arity$1(opts);
var system_options = figwheel.client.handle_deprecated_jsload_callback.call(null,cljs.core.merge.call(null,figwheel.client.config_defaults,cljs.core.dissoc.call(null,opts,new cljs.core.Keyword(null,"plugins","plugins",1900073717),new cljs.core.Keyword(null,"merge-plugins","merge-plugins",-1193912370))));
var plugins = (cljs.core.truth_(plugins_SINGLEQUOTE_)?plugins_SINGLEQUOTE_:cljs.core.merge.call(null,figwheel.client.base_plugins.call(null,system_options),merge_plugins));
figwheel.client.utils._STAR_print_debug_STAR_ = new cljs.core.Keyword(null,"debug","debug",-1608172596).cljs$core$IFn$_invoke$arity$1(opts);

figwheel.client.add_plugins.call(null,plugins,system_options);

figwheel.client.file_reloading.patch_goog_base.call(null);

return figwheel.client.socket.open.call(null,system_options);
}));
}
}
});

figwheel.client.start.cljs$core$IFn$_invoke$arity$0 = (function (){
return figwheel.client.start.call(null,cljs.core.PersistentArrayMap.EMPTY);
});

figwheel.client.start.cljs$lang$maxFixedArity = 1;
figwheel.client.watch_and_reload_with_opts = figwheel.client.start;
figwheel.client.watch_and_reload = (function figwheel$client$watch_and_reload(){
var argseq__17109__auto__ = ((((0) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(0)),(0))):null);
return figwheel.client.watch_and_reload.cljs$core$IFn$_invoke$arity$variadic(argseq__17109__auto__);
});

figwheel.client.watch_and_reload.cljs$core$IFn$_invoke$arity$variadic = (function (p__42002){
var map__42003 = p__42002;
var map__42003__$1 = ((cljs.core.seq_QMARK_.call(null,map__42003))?cljs.core.apply.call(null,cljs.core.hash_map,map__42003):map__42003);
var opts = map__42003__$1;
return figwheel.client.start.call(null,opts);
});

figwheel.client.watch_and_reload.cljs$lang$maxFixedArity = (0);

figwheel.client.watch_and_reload.cljs$lang$applyTo = (function (seq42001){
return figwheel.client.watch_and_reload.cljs$core$IFn$_invoke$arity$variadic(cljs.core.seq.call(null,seq42001));
});

//# sourceMappingURL=client.js.map?rel=1439206059147
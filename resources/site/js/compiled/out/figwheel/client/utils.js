// Compiled by ClojureScript 0.0-3297 {}
goog.provide('figwheel.client.utils');
goog.require('cljs.core');
goog.require('clojure.string');
figwheel.client.utils._STAR_print_debug_STAR_ = false;
figwheel.client.utils.html_env_QMARK_ = (function figwheel$client$utils$html_env_QMARK_(){
return goog.inHtmlDocument_();
});
figwheel.client.utils.node_env_QMARK_ = (function figwheel$client$utils$node_env_QMARK_(){
return !((goog.nodeGlobalRequire == null));
});
figwheel.client.utils.base_url_path = (function figwheel$client$utils$base_url_path(){
return clojure.string.replace.call(null,goog.basePath,/(.*)goog\//,(function (p1__32409_SHARP_,p2__32408_SHARP_){
return [cljs.core.str(p2__32408_SHARP_)].join('');
}));
});
figwheel.client.utils.host_env_QMARK_ = (function figwheel$client$utils$host_env_QMARK_(){
if(cljs.core.truth_(figwheel.client.utils.node_env_QMARK_.call(null))){
return new cljs.core.Keyword(null,"node","node",581201198);
} else {
return new cljs.core.Keyword(null,"html","html",-998796897);

}
});
figwheel.client.utils.dispatch_custom_event = (function figwheel$client$utils$dispatch_custom_event(event_name,data){
if(cljs.core.truth_((function (){var and__16057__auto__ = figwheel.client.utils.html_env_QMARK_.call(null);
if(cljs.core.truth_(and__16057__auto__)){
return (window["CustomEvent"]);
} else {
return and__16057__auto__;
}
})())){
return document.body.dispatchEvent((new CustomEvent(event_name,(function (){var obj32413 = {"detail":data};
return obj32413;
})())));
} else {
return null;
}
});
figwheel.client.utils.debug_prn = (function figwheel$client$utils$debug_prn(o){
if(cljs.core.truth_(figwheel.client.utils._STAR_print_debug_STAR_)){
var o__$1 = (((cljs.core.map_QMARK_.call(null,o)) || (cljs.core.seq_QMARK_.call(null,o)))?cljs.core.prn_str.call(null,o):o);
return console.log(o__$1);
} else {
return null;
}
});
figwheel.client.utils.log = (function figwheel$client$utils$log(){
var G__32419 = arguments.length;
switch (G__32419) {
case 1:
return figwheel.client.utils.log.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return figwheel.client.utils.log.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

figwheel.client.utils.log.cljs$core$IFn$_invoke$arity$1 = (function (x){
return figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"info","info",-317069002),x);
});

figwheel.client.utils.log.cljs$core$IFn$_invoke$arity$2 = (function (level,arg){
var f = (function (){var pred__32420 = cljs.core._EQ_;
var expr__32421 = (cljs.core.truth_(figwheel.client.utils.html_env_QMARK_.call(null))?level:new cljs.core.Keyword(null,"info","info",-317069002));
if(cljs.core.truth_(pred__32420.call(null,new cljs.core.Keyword(null,"warn","warn",-436710552),expr__32421))){
return ((function (pred__32420,expr__32421){
return (function (p1__32414_SHARP_){
return console.warn(p1__32414_SHARP_);
});
;})(pred__32420,expr__32421))
} else {
if(cljs.core.truth_(pred__32420.call(null,new cljs.core.Keyword(null,"debug","debug",-1608172596),expr__32421))){
return ((function (pred__32420,expr__32421){
return (function (p1__32415_SHARP_){
return console.debug(p1__32415_SHARP_);
});
;})(pred__32420,expr__32421))
} else {
if(cljs.core.truth_(pred__32420.call(null,new cljs.core.Keyword(null,"error","error",-978969032),expr__32421))){
return ((function (pred__32420,expr__32421){
return (function (p1__32416_SHARP_){
return console.error(p1__32416_SHARP_);
});
;})(pred__32420,expr__32421))
} else {
return ((function (pred__32420,expr__32421){
return (function (p1__32417_SHARP_){
return console.log(p1__32417_SHARP_);
});
;})(pred__32420,expr__32421))
}
}
}
})();
return f.call(null,arg);
});

figwheel.client.utils.log.cljs$lang$maxFixedArity = 2;
figwheel.client.utils.eval_helper = (function figwheel$client$utils$eval_helper(code,p__32424){
var map__32426 = p__32424;
var map__32426__$1 = ((cljs.core.seq_QMARK_.call(null,map__32426))?cljs.core.apply.call(null,cljs.core.hash_map,map__32426):map__32426);
var opts = map__32426__$1;
var eval_fn = cljs.core.get.call(null,map__32426__$1,new cljs.core.Keyword(null,"eval-fn","eval-fn",-1111644294));
if(cljs.core.truth_(eval_fn)){
return eval_fn.call(null,code,opts);
} else {
return eval(code);
}
});

//# sourceMappingURL=utils.js.map?rel=1439206039275
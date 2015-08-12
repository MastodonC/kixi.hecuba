// Compiled by ClojureScript 0.0-3297 {}
goog.provide('figwheel.connect');
goog.require('cljs.core');
goog.require('geom_om.core');
goog.require('figwheel.client');
goog.require('figwheel.client.utils');
figwheel.client.start.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"build-id","build-id",1642831089),"dev",new cljs.core.Keyword(null,"on-jsload","on-jsload",-395756602),(function() { 
var G__43041__delegate = function (x){
if(cljs.core.truth_(geom_om.core.on_js_reload)){
return cljs.core.apply.call(null,geom_om.core.on_js_reload,x);
} else {
return figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"debug","debug",-1608172596),"Figwheel: :on-jsload hook 'geom-om.core/on-js-reload' is missing");
}
};
var G__43041 = function (var_args){
var x = null;
if (arguments.length > 0) {
var G__43042__i = 0, G__43042__a = new Array(arguments.length -  0);
while (G__43042__i < G__43042__a.length) {G__43042__a[G__43042__i] = arguments[G__43042__i + 0]; ++G__43042__i;}
  x = new cljs.core.IndexedSeq(G__43042__a,0);
} 
return G__43041__delegate.call(this,x);};
G__43041.cljs$lang$maxFixedArity = 0;
G__43041.cljs$lang$applyTo = (function (arglist__43043){
var x = cljs.core.seq(arglist__43043);
return G__43041__delegate(x);
});
G__43041.cljs$core$IFn$_invoke$arity$variadic = G__43041__delegate;
return G__43041;
})()
,new cljs.core.Keyword(null,"websocket-url","websocket-url",-490444938),"ws://localhost:3449/figwheel-ws"], null));

//# sourceMappingURL=connect.js.map?rel=1439206061068
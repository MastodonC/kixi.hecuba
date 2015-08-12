// Compiled by ClojureScript 0.0-3297 {}
goog.provide('cljs.repl');
goog.require('cljs.core');
cljs.repl.print_doc = (function cljs$repl$print_doc(m){
cljs.core.println.call(null,"-------------------------");

cljs.core.println.call(null,[cljs.core.str((function (){var temp__4425__auto__ = new cljs.core.Keyword(null,"ns","ns",441598760).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(temp__4425__auto__)){
var ns = temp__4425__auto__;
return [cljs.core.str(ns),cljs.core.str("/")].join('');
} else {
return null;
}
})()),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Protocol");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m))){
var seq__42259_42271 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m));
var chunk__42260_42272 = null;
var count__42261_42273 = (0);
var i__42262_42274 = (0);
while(true){
if((i__42262_42274 < count__42261_42273)){
var f_42275 = cljs.core._nth.call(null,chunk__42260_42272,i__42262_42274);
cljs.core.println.call(null,"  ",f_42275);

var G__42276 = seq__42259_42271;
var G__42277 = chunk__42260_42272;
var G__42278 = count__42261_42273;
var G__42279 = (i__42262_42274 + (1));
seq__42259_42271 = G__42276;
chunk__42260_42272 = G__42277;
count__42261_42273 = G__42278;
i__42262_42274 = G__42279;
continue;
} else {
var temp__4425__auto___42280 = cljs.core.seq.call(null,seq__42259_42271);
if(temp__4425__auto___42280){
var seq__42259_42281__$1 = temp__4425__auto___42280;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__42259_42281__$1)){
var c__16854__auto___42282 = cljs.core.chunk_first.call(null,seq__42259_42281__$1);
var G__42283 = cljs.core.chunk_rest.call(null,seq__42259_42281__$1);
var G__42284 = c__16854__auto___42282;
var G__42285 = cljs.core.count.call(null,c__16854__auto___42282);
var G__42286 = (0);
seq__42259_42271 = G__42283;
chunk__42260_42272 = G__42284;
count__42261_42273 = G__42285;
i__42262_42274 = G__42286;
continue;
} else {
var f_42287 = cljs.core.first.call(null,seq__42259_42281__$1);
cljs.core.println.call(null,"  ",f_42287);

var G__42288 = cljs.core.next.call(null,seq__42259_42281__$1);
var G__42289 = null;
var G__42290 = (0);
var G__42291 = (0);
seq__42259_42271 = G__42288;
chunk__42260_42272 = G__42289;
count__42261_42273 = G__42290;
i__42262_42274 = G__42291;
continue;
}
} else {
}
}
break;
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m))){
var arglists_42292 = new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_((function (){var or__16069__auto__ = new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m);
}
})())){
cljs.core.prn.call(null,arglists_42292);
} else {
cljs.core.prn.call(null,((cljs.core._EQ_.call(null,new cljs.core.Symbol(null,"quote","quote",1377916282,null),cljs.core.first.call(null,arglists_42292)))?cljs.core.second.call(null,arglists_42292):arglists_42292));
}
} else {
}
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"special-form","special-form",-1326536374).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Special Form");

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.contains_QMARK_.call(null,m,new cljs.core.Keyword(null,"url","url",276297046))){
if(cljs.core.truth_(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))){
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/"),cljs.core.str(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))].join(''));
} else {
return null;
}
} else {
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/special_forms#"),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Macro");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"REPL Special Function");
} else {
}

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
var seq__42263 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"methods","methods",453930866).cljs$core$IFn$_invoke$arity$1(m));
var chunk__42264 = null;
var count__42265 = (0);
var i__42266 = (0);
while(true){
if((i__42266 < count__42265)){
var vec__42267 = cljs.core._nth.call(null,chunk__42264,i__42266);
var name = cljs.core.nth.call(null,vec__42267,(0),null);
var map__42268 = cljs.core.nth.call(null,vec__42267,(1),null);
var map__42268__$1 = ((cljs.core.seq_QMARK_.call(null,map__42268))?cljs.core.apply.call(null,cljs.core.hash_map,map__42268):map__42268);
var doc = cljs.core.get.call(null,map__42268__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists = cljs.core.get.call(null,map__42268__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name);

cljs.core.println.call(null," ",arglists);

if(cljs.core.truth_(doc)){
cljs.core.println.call(null," ",doc);
} else {
}

var G__42293 = seq__42263;
var G__42294 = chunk__42264;
var G__42295 = count__42265;
var G__42296 = (i__42266 + (1));
seq__42263 = G__42293;
chunk__42264 = G__42294;
count__42265 = G__42295;
i__42266 = G__42296;
continue;
} else {
var temp__4425__auto__ = cljs.core.seq.call(null,seq__42263);
if(temp__4425__auto__){
var seq__42263__$1 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__42263__$1)){
var c__16854__auto__ = cljs.core.chunk_first.call(null,seq__42263__$1);
var G__42297 = cljs.core.chunk_rest.call(null,seq__42263__$1);
var G__42298 = c__16854__auto__;
var G__42299 = cljs.core.count.call(null,c__16854__auto__);
var G__42300 = (0);
seq__42263 = G__42297;
chunk__42264 = G__42298;
count__42265 = G__42299;
i__42266 = G__42300;
continue;
} else {
var vec__42269 = cljs.core.first.call(null,seq__42263__$1);
var name = cljs.core.nth.call(null,vec__42269,(0),null);
var map__42270 = cljs.core.nth.call(null,vec__42269,(1),null);
var map__42270__$1 = ((cljs.core.seq_QMARK_.call(null,map__42270))?cljs.core.apply.call(null,cljs.core.hash_map,map__42270):map__42270);
var doc = cljs.core.get.call(null,map__42270__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists = cljs.core.get.call(null,map__42270__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name);

cljs.core.println.call(null," ",arglists);

if(cljs.core.truth_(doc)){
cljs.core.println.call(null," ",doc);
} else {
}

var G__42301 = cljs.core.next.call(null,seq__42263__$1);
var G__42302 = null;
var G__42303 = (0);
var G__42304 = (0);
seq__42263 = G__42301;
chunk__42264 = G__42302;
count__42265 = G__42303;
i__42266 = G__42304;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return null;
}
}
});

//# sourceMappingURL=repl.js.map?rel=1439206059469
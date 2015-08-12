// Compiled by ClojureScript 0.0-3297 {}
goog.provide('figwheel.client.file_reloading');
goog.require('cljs.core');
goog.require('goog.Uri');
goog.require('goog.string');
goog.require('goog.net.jsloader');
goog.require('cljs.core.async');
goog.require('clojure.set');
goog.require('clojure.string');
goog.require('figwheel.client.utils');

figwheel.client.file_reloading.on_jsload_custom_event = (function figwheel$client$file_reloading$on_jsload_custom_event(url){
return figwheel.client.utils.dispatch_custom_event.call(null,"figwheel.js-reload",url);
});
figwheel.client.file_reloading.before_jsload_custom_event = (function figwheel$client$file_reloading$before_jsload_custom_event(files){
return figwheel.client.utils.dispatch_custom_event.call(null,"figwheel.before-js-reload",files);
});
figwheel.client.file_reloading.all_QMARK_ = (function figwheel$client$file_reloading$all_QMARK_(pred,coll){
return cljs.core.reduce.call(null,(function (p1__42307_SHARP_,p2__42308_SHARP_){
var and__16057__auto__ = p1__42307_SHARP_;
if(cljs.core.truth_(and__16057__auto__)){
return p2__42308_SHARP_;
} else {
return and__16057__auto__;
}
}),true,cljs.core.map.call(null,pred,coll));
});
figwheel.client.file_reloading.namespace_file_map_QMARK_ = (function figwheel$client$file_reloading$namespace_file_map_QMARK_(m){
var or__16069__auto__ = (cljs.core.map_QMARK_.call(null,m)) && (typeof new cljs.core.Keyword(null,"namespace","namespace",-377510372).cljs$core$IFn$_invoke$arity$1(m) === 'string') && (typeof new cljs.core.Keyword(null,"file","file",-1269645878).cljs$core$IFn$_invoke$arity$1(m) === 'string') && (cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"type","type",1174270348).cljs$core$IFn$_invoke$arity$1(m),new cljs.core.Keyword(null,"namespace","namespace",-377510372)));
if(or__16069__auto__){
return or__16069__auto__;
} else {
cljs.core.println.call(null,"Error not namespace-file-map",cljs.core.pr_str.call(null,m));

return false;
}
});
figwheel.client.file_reloading.add_cache_buster = (function figwheel$client$file_reloading$add_cache_buster(url){

return goog.Uri.parse(url).makeUnique();
});
figwheel.client.file_reloading.ns_to_js_file = (function figwheel$client$file_reloading$ns_to_js_file(ns){

return [cljs.core.str(clojure.string.replace.call(null,ns,".","/")),cljs.core.str(".js")].join('');
});
figwheel.client.file_reloading.resolve_ns = (function figwheel$client$file_reloading$resolve_ns(ns){

return [cljs.core.str(figwheel.client.utils.base_url_path.call(null)),cljs.core.str(figwheel.client.file_reloading.ns_to_js_file.call(null,ns))].join('');
});
figwheel.client.file_reloading.patch_goog_base = (function figwheel$client$file_reloading$patch_goog_base(){
goog.isProvided = (function (x){
return false;
});

if(((cljs.core._STAR_loaded_libs_STAR_ == null)) || (cljs.core.empty_QMARK_.call(null,cljs.core._STAR_loaded_libs_STAR_))){
cljs.core._STAR_loaded_libs_STAR_ = (function (){var gntp = goog.dependencies_.nameToPath;
return cljs.core.into.call(null,cljs.core.PersistentHashSet.EMPTY,cljs.core.filter.call(null,((function (gntp){
return (function (name){
return (goog.dependencies_.visited[(gntp[name])]);
});})(gntp))
,cljs.core.js_keys.call(null,gntp)));
})();
} else {
}

goog.require = (function (name,reload){
if(cljs.core.truth_((function (){var or__16069__auto__ = !(cljs.core.contains_QMARK_.call(null,cljs.core._STAR_loaded_libs_STAR_,name));
if(or__16069__auto__){
return or__16069__auto__;
} else {
return reload;
}
})())){
cljs.core._STAR_loaded_libs_STAR_ = cljs.core.conj.call(null,(function (){var or__16069__auto__ = cljs.core._STAR_loaded_libs_STAR_;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return cljs.core.PersistentHashSet.EMPTY;
}
})(),name);

return figwheel.client.file_reloading.reload_file_STAR_.call(null,figwheel.client.file_reloading.resolve_ns.call(null,name));
} else {
return null;
}
});

goog.provide = goog.exportPath_;

return goog.global.CLOSURE_IMPORT_SCRIPT = figwheel.client.file_reloading.reload_file_STAR_;
});
if(typeof figwheel.client.file_reloading.resolve_url !== 'undefined'){
} else {
figwheel.client.file_reloading.resolve_url = (function (){var method_table__16964__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var prefer_table__16965__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var method_cache__16966__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var cached_hierarchy__16967__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var hierarchy__16968__auto__ = cljs.core.get.call(null,cljs.core.PersistentArrayMap.EMPTY,new cljs.core.Keyword(null,"hierarchy","hierarchy",-1053470341),cljs.core.get_global_hierarchy.call(null));
return (new cljs.core.MultiFn(cljs.core.symbol.call(null,"figwheel.client.file-reloading","resolve-url"),new cljs.core.Keyword(null,"type","type",1174270348),new cljs.core.Keyword(null,"default","default",-1987822328),hierarchy__16968__auto__,method_table__16964__auto__,prefer_table__16965__auto__,method_cache__16966__auto__,cached_hierarchy__16967__auto__));
})();
}
cljs.core._add_method.call(null,figwheel.client.file_reloading.resolve_url,new cljs.core.Keyword(null,"default","default",-1987822328),(function (p__42309){
var map__42310 = p__42309;
var map__42310__$1 = ((cljs.core.seq_QMARK_.call(null,map__42310))?cljs.core.apply.call(null,cljs.core.hash_map,map__42310):map__42310);
var file = cljs.core.get.call(null,map__42310__$1,new cljs.core.Keyword(null,"file","file",-1269645878));
return file;
}));
cljs.core._add_method.call(null,figwheel.client.file_reloading.resolve_url,new cljs.core.Keyword(null,"namespace","namespace",-377510372),(function (p__42311){
var map__42312 = p__42311;
var map__42312__$1 = ((cljs.core.seq_QMARK_.call(null,map__42312))?cljs.core.apply.call(null,cljs.core.hash_map,map__42312):map__42312);
var namespace = cljs.core.get.call(null,map__42312__$1,new cljs.core.Keyword(null,"namespace","namespace",-377510372));

return figwheel.client.file_reloading.resolve_ns.call(null,namespace);
}));
if(typeof figwheel.client.file_reloading.reload_base !== 'undefined'){
} else {
figwheel.client.file_reloading.reload_base = (function (){var method_table__16964__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var prefer_table__16965__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var method_cache__16966__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var cached_hierarchy__16967__auto__ = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var hierarchy__16968__auto__ = cljs.core.get.call(null,cljs.core.PersistentArrayMap.EMPTY,new cljs.core.Keyword(null,"hierarchy","hierarchy",-1053470341),cljs.core.get_global_hierarchy.call(null));
return (new cljs.core.MultiFn(cljs.core.symbol.call(null,"figwheel.client.file-reloading","reload-base"),figwheel.client.utils.host_env_QMARK_,new cljs.core.Keyword(null,"default","default",-1987822328),hierarchy__16968__auto__,method_table__16964__auto__,prefer_table__16965__auto__,method_cache__16966__auto__,cached_hierarchy__16967__auto__));
})();
}
cljs.core._add_method.call(null,figwheel.client.file_reloading.reload_base,new cljs.core.Keyword(null,"node","node",581201198),(function (request_url,callback){

var root = clojure.string.join.call(null,"/",cljs.core.reverse.call(null,cljs.core.drop.call(null,(2),cljs.core.reverse.call(null,clojure.string.split.call(null,__dirname,"/")))));
var path = [cljs.core.str(root),cljs.core.str("/"),cljs.core.str(request_url)].join('');
(require.cache[path] = null);

return callback.call(null,(function (){try{return require(path);
}catch (e42313){if((e42313 instanceof Error)){
var e = e42313;
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"error","error",-978969032),[cljs.core.str("Figwheel: Error loading file "),cljs.core.str(path)].join(''));

figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"error","error",-978969032),e.stack);

return false;
} else {
throw e42313;

}
}})());
}));
cljs.core._add_method.call(null,figwheel.client.file_reloading.reload_base,new cljs.core.Keyword(null,"html","html",-998796897),(function (request_url,callback){

var deferred = goog.net.jsloader.load(figwheel.client.file_reloading.add_cache_buster.call(null,request_url),{"cleanupWhenDone": true});
deferred.addCallback(((function (deferred){
return (function (){
return cljs.core.apply.call(null,callback,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [true], null));
});})(deferred))
);

return deferred.addErrback(((function (deferred){
return (function (){
return cljs.core.apply.call(null,callback,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [false], null));
});})(deferred))
);
}));
figwheel.client.file_reloading.reload_file_STAR_ = (function figwheel$client$file_reloading$reload_file_STAR_(){
var G__42315 = arguments.length;
switch (G__42315) {
case 2:
return figwheel.client.file_reloading.reload_file_STAR_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 1:
return figwheel.client.file_reloading.reload_file_STAR_.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

figwheel.client.file_reloading.reload_file_STAR_.cljs$core$IFn$_invoke$arity$2 = (function (request_url,callback){
return figwheel.client.file_reloading.reload_base.call(null,request_url,callback);
});

figwheel.client.file_reloading.reload_file_STAR_.cljs$core$IFn$_invoke$arity$1 = (function (request_url){
return figwheel.client.file_reloading.reload_file_STAR_.call(null,request_url,cljs.core.identity);
});

figwheel.client.file_reloading.reload_file_STAR_.cljs$lang$maxFixedArity = 2;
figwheel.client.file_reloading.reload_file = (function figwheel$client$file_reloading$reload_file(p__42317,callback){
var map__42319 = p__42317;
var map__42319__$1 = ((cljs.core.seq_QMARK_.call(null,map__42319))?cljs.core.apply.call(null,cljs.core.hash_map,map__42319):map__42319);
var file_msg = map__42319__$1;
var request_url = cljs.core.get.call(null,map__42319__$1,new cljs.core.Keyword(null,"request-url","request-url",2100346596));

figwheel.client.utils.debug_prn.call(null,[cljs.core.str("FigWheel: Attempting to load "),cljs.core.str(request_url)].join(''));

return figwheel.client.file_reloading.reload_file_STAR_.call(null,request_url,((function (map__42319,map__42319__$1,file_msg,request_url){
return (function (success_QMARK_){
if(cljs.core.truth_(success_QMARK_)){
figwheel.client.utils.debug_prn.call(null,[cljs.core.str("FigWheel: Successfullly loaded "),cljs.core.str(request_url)].join(''));

return cljs.core.apply.call(null,callback,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.assoc.call(null,file_msg,new cljs.core.Keyword(null,"loaded-file","loaded-file",-168399375),true)], null));
} else {
figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"error","error",-978969032),[cljs.core.str("Figwheel: Error loading file "),cljs.core.str(request_url)].join(''));

return cljs.core.apply.call(null,callback,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [file_msg], null));
}
});})(map__42319,map__42319__$1,file_msg,request_url))
);
});
figwheel.client.file_reloading.reload_file_QMARK_ = (function figwheel$client$file_reloading$reload_file_QMARK_(p__42320){
var map__42322 = p__42320;
var map__42322__$1 = ((cljs.core.seq_QMARK_.call(null,map__42322))?cljs.core.apply.call(null,cljs.core.hash_map,map__42322):map__42322);
var file_msg = map__42322__$1;
var namespace = cljs.core.get.call(null,map__42322__$1,new cljs.core.Keyword(null,"namespace","namespace",-377510372));
var meta_data = cljs.core.get.call(null,map__42322__$1,new cljs.core.Keyword(null,"meta-data","meta-data",-1613399157));

var meta_data__$1 = (function (){var or__16069__auto__ = meta_data;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return cljs.core.PersistentArrayMap.EMPTY;
}
})();
var and__16057__auto__ = cljs.core.not.call(null,new cljs.core.Keyword(null,"figwheel-no-load","figwheel-no-load",-555840179).cljs$core$IFn$_invoke$arity$1(meta_data__$1));
if(and__16057__auto__){
var or__16069__auto__ = new cljs.core.Keyword(null,"figwheel-always","figwheel-always",799819691).cljs$core$IFn$_invoke$arity$1(meta_data__$1);
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = new cljs.core.Keyword(null,"figwheel-load","figwheel-load",1316089175).cljs$core$IFn$_invoke$arity$1(meta_data__$1);
if(cljs.core.truth_(or__16069__auto____$1)){
return or__16069__auto____$1;
} else {
var and__16057__auto____$1 = cljs.core.contains_QMARK_.call(null,cljs.core._STAR_loaded_libs_STAR_,namespace);
if(and__16057__auto____$1){
var or__16069__auto____$2 = !(cljs.core.contains_QMARK_.call(null,meta_data__$1,new cljs.core.Keyword(null,"file-changed-on-disk","file-changed-on-disk",1086171932)));
if(or__16069__auto____$2){
return or__16069__auto____$2;
} else {
return new cljs.core.Keyword(null,"file-changed-on-disk","file-changed-on-disk",1086171932).cljs$core$IFn$_invoke$arity$1(meta_data__$1);
}
} else {
return and__16057__auto____$1;
}
}
}
} else {
return and__16057__auto__;
}
});
figwheel.client.file_reloading.js_reload = (function figwheel$client$file_reloading$js_reload(p__42323,callback){
var map__42325 = p__42323;
var map__42325__$1 = ((cljs.core.seq_QMARK_.call(null,map__42325))?cljs.core.apply.call(null,cljs.core.hash_map,map__42325):map__42325);
var file_msg = map__42325__$1;
var request_url = cljs.core.get.call(null,map__42325__$1,new cljs.core.Keyword(null,"request-url","request-url",2100346596));
var namespace = cljs.core.get.call(null,map__42325__$1,new cljs.core.Keyword(null,"namespace","namespace",-377510372));

if(cljs.core.truth_(figwheel.client.file_reloading.reload_file_QMARK_.call(null,file_msg))){
return figwheel.client.file_reloading.reload_file.call(null,file_msg,callback);
} else {
figwheel.client.utils.debug_prn.call(null,[cljs.core.str("Figwheel: Not trying to load file "),cljs.core.str(request_url)].join(''));

return cljs.core.apply.call(null,callback,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [file_msg], null));
}
});
figwheel.client.file_reloading.reload_js_file = (function figwheel$client$file_reloading$reload_js_file(file_msg){
var out = cljs.core.async.chan.call(null);
setTimeout(((function (out){
return (function (){
return figwheel.client.file_reloading.js_reload.call(null,file_msg,((function (out){
return (function (url){
figwheel.client.file_reloading.patch_goog_base.call(null);

cljs.core.async.put_BANG_.call(null,out,url);

return cljs.core.async.close_BANG_.call(null,out);
});})(out))
);
});})(out))
,(0));

return out;
});
/**
 * Returns a chanel with one collection of loaded filenames on it.
 */
figwheel.client.file_reloading.load_all_js_files = (function figwheel$client$file_reloading$load_all_js_files(files){
var out = cljs.core.async.chan.call(null);
var c__23633__auto___42412 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___42412,out){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___42412,out){
return (function (state_42394){
var state_val_42395 = (state_42394[(1)]);
if((state_val_42395 === (1))){
var inst_42372 = cljs.core.nth.call(null,files,(0),null);
var inst_42373 = cljs.core.nthnext.call(null,files,(1));
var inst_42374 = files;
var state_42394__$1 = (function (){var statearr_42396 = state_42394;
(statearr_42396[(7)] = inst_42372);

(statearr_42396[(8)] = inst_42373);

(statearr_42396[(9)] = inst_42374);

return statearr_42396;
})();
var statearr_42397_42413 = state_42394__$1;
(statearr_42397_42413[(2)] = null);

(statearr_42397_42413[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42395 === (2))){
var inst_42374 = (state_42394[(9)]);
var inst_42377 = (state_42394[(10)]);
var inst_42377__$1 = cljs.core.nth.call(null,inst_42374,(0),null);
var inst_42378 = cljs.core.nthnext.call(null,inst_42374,(1));
var inst_42379 = (inst_42377__$1 == null);
var inst_42380 = cljs.core.not.call(null,inst_42379);
var state_42394__$1 = (function (){var statearr_42398 = state_42394;
(statearr_42398[(11)] = inst_42378);

(statearr_42398[(10)] = inst_42377__$1);

return statearr_42398;
})();
if(inst_42380){
var statearr_42399_42414 = state_42394__$1;
(statearr_42399_42414[(1)] = (4));

} else {
var statearr_42400_42415 = state_42394__$1;
(statearr_42400_42415[(1)] = (5));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42395 === (3))){
var inst_42392 = (state_42394[(2)]);
var state_42394__$1 = state_42394;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_42394__$1,inst_42392);
} else {
if((state_val_42395 === (4))){
var inst_42377 = (state_42394[(10)]);
var inst_42382 = figwheel.client.file_reloading.reload_js_file.call(null,inst_42377);
var state_42394__$1 = state_42394;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42394__$1,(7),inst_42382);
} else {
if((state_val_42395 === (5))){
var inst_42388 = cljs.core.async.close_BANG_.call(null,out);
var state_42394__$1 = state_42394;
var statearr_42401_42416 = state_42394__$1;
(statearr_42401_42416[(2)] = inst_42388);

(statearr_42401_42416[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42395 === (6))){
var inst_42390 = (state_42394[(2)]);
var state_42394__$1 = state_42394;
var statearr_42402_42417 = state_42394__$1;
(statearr_42402_42417[(2)] = inst_42390);

(statearr_42402_42417[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42395 === (7))){
var inst_42378 = (state_42394[(11)]);
var inst_42384 = (state_42394[(2)]);
var inst_42385 = cljs.core.async.put_BANG_.call(null,out,inst_42384);
var inst_42374 = inst_42378;
var state_42394__$1 = (function (){var statearr_42403 = state_42394;
(statearr_42403[(12)] = inst_42385);

(statearr_42403[(9)] = inst_42374);

return statearr_42403;
})();
var statearr_42404_42418 = state_42394__$1;
(statearr_42404_42418[(2)] = null);

(statearr_42404_42418[(1)] = (2));


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
});})(c__23633__auto___42412,out))
;
return ((function (switch__23571__auto__,c__23633__auto___42412,out){
return (function() {
var figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto__ = null;
var figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto____0 = (function (){
var statearr_42408 = [null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_42408[(0)] = figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto__);

(statearr_42408[(1)] = (1));

return statearr_42408;
});
var figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto____1 = (function (state_42394){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_42394);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e42409){if((e42409 instanceof Object)){
var ex__23575__auto__ = e42409;
var statearr_42410_42419 = state_42394;
(statearr_42410_42419[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_42394);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e42409;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__42420 = state_42394;
state_42394 = G__42420;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto__ = function(state_42394){
switch(arguments.length){
case 0:
return figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto____1.call(this,state_42394);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto____0;
figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto____1;
return figwheel$client$file_reloading$load_all_js_files_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___42412,out))
})();
var state__23635__auto__ = (function (){var statearr_42411 = f__23634__auto__.call(null);
(statearr_42411[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___42412);

return statearr_42411;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___42412,out))
);


return cljs.core.async.into.call(null,cljs.core.PersistentVector.EMPTY,out);
});
figwheel.client.file_reloading.add_request_url = (function figwheel$client$file_reloading$add_request_url(p__42421,p__42422){
var map__42425 = p__42421;
var map__42425__$1 = ((cljs.core.seq_QMARK_.call(null,map__42425))?cljs.core.apply.call(null,cljs.core.hash_map,map__42425):map__42425);
var opts = map__42425__$1;
var url_rewriter = cljs.core.get.call(null,map__42425__$1,new cljs.core.Keyword(null,"url-rewriter","url-rewriter",200543838));
var map__42426 = p__42422;
var map__42426__$1 = ((cljs.core.seq_QMARK_.call(null,map__42426))?cljs.core.apply.call(null,cljs.core.hash_map,map__42426):map__42426);
var file_msg = map__42426__$1;
var file = cljs.core.get.call(null,map__42426__$1,new cljs.core.Keyword(null,"file","file",-1269645878));
var resolved_path = figwheel.client.file_reloading.resolve_url.call(null,file_msg);
return cljs.core.assoc.call(null,file_msg,new cljs.core.Keyword(null,"request-url","request-url",2100346596),(cljs.core.truth_(url_rewriter)?url_rewriter.call(null,resolved_path):resolved_path));
});
figwheel.client.file_reloading.add_request_urls = (function figwheel$client$file_reloading$add_request_urls(opts,files){
return cljs.core.map.call(null,cljs.core.partial.call(null,figwheel.client.file_reloading.add_request_url,opts),files);
});
figwheel.client.file_reloading.eval_body = (function figwheel$client$file_reloading$eval_body(p__42427,opts){
var map__42430 = p__42427;
var map__42430__$1 = ((cljs.core.seq_QMARK_.call(null,map__42430))?cljs.core.apply.call(null,cljs.core.hash_map,map__42430):map__42430);
var eval_body__$1 = cljs.core.get.call(null,map__42430__$1,new cljs.core.Keyword(null,"eval-body","eval-body",-907279883));
var file = cljs.core.get.call(null,map__42430__$1,new cljs.core.Keyword(null,"file","file",-1269645878));
if(cljs.core.truth_((function (){var and__16057__auto__ = eval_body__$1;
if(cljs.core.truth_(and__16057__auto__)){
return typeof eval_body__$1 === 'string';
} else {
return and__16057__auto__;
}
})())){
var code = eval_body__$1;
try{figwheel.client.utils.debug_prn.call(null,[cljs.core.str("Evaling file "),cljs.core.str(file)].join(''));

return figwheel.client.utils.eval_helper.call(null,code,opts);
}catch (e42431){var e = e42431;
return figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"error","error",-978969032),[cljs.core.str("Unable to evaluate "),cljs.core.str(file)].join(''));
}} else {
return null;
}
});
figwheel.client.file_reloading.reload_js_files = (function figwheel$client$file_reloading$reload_js_files(p__42436,p__42437){
var map__42639 = p__42436;
var map__42639__$1 = ((cljs.core.seq_QMARK_.call(null,map__42639))?cljs.core.apply.call(null,cljs.core.hash_map,map__42639):map__42639);
var opts = map__42639__$1;
var before_jsload = cljs.core.get.call(null,map__42639__$1,new cljs.core.Keyword(null,"before-jsload","before-jsload",-847513128));
var on_jsload = cljs.core.get.call(null,map__42639__$1,new cljs.core.Keyword(null,"on-jsload","on-jsload",-395756602));
var load_unchanged_files = cljs.core.get.call(null,map__42639__$1,new cljs.core.Keyword(null,"load-unchanged-files","load-unchanged-files",-1561468704));
var map__42640 = p__42437;
var map__42640__$1 = ((cljs.core.seq_QMARK_.call(null,map__42640))?cljs.core.apply.call(null,cljs.core.hash_map,map__42640):map__42640);
var msg = map__42640__$1;
var files = cljs.core.get.call(null,map__42640__$1,new cljs.core.Keyword(null,"files","files",-472457450));
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (state_42765){
var state_val_42766 = (state_42765[(1)]);
if((state_val_42766 === (7))){
var inst_42652 = (state_42765[(7)]);
var inst_42654 = (state_42765[(8)]);
var inst_42655 = (state_42765[(9)]);
var inst_42653 = (state_42765[(10)]);
var inst_42660 = cljs.core._nth.call(null,inst_42653,inst_42655);
var inst_42661 = figwheel.client.file_reloading.eval_body.call(null,inst_42660,opts);
var inst_42662 = (inst_42655 + (1));
var tmp42767 = inst_42652;
var tmp42768 = inst_42654;
var tmp42769 = inst_42653;
var inst_42652__$1 = tmp42767;
var inst_42653__$1 = tmp42769;
var inst_42654__$1 = tmp42768;
var inst_42655__$1 = inst_42662;
var state_42765__$1 = (function (){var statearr_42770 = state_42765;
(statearr_42770[(7)] = inst_42652__$1);

(statearr_42770[(8)] = inst_42654__$1);

(statearr_42770[(9)] = inst_42655__$1);

(statearr_42770[(10)] = inst_42653__$1);

(statearr_42770[(11)] = inst_42661);

return statearr_42770;
})();
var statearr_42771_42840 = state_42765__$1;
(statearr_42771_42840[(2)] = null);

(statearr_42771_42840[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (20))){
var inst_42704 = (state_42765[(12)]);
var inst_42697 = (state_42765[(13)]);
var inst_42698 = (state_42765[(14)]);
var inst_42702 = (state_42765[(15)]);
var inst_42701 = (state_42765[(16)]);
var inst_42707 = figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"debug","debug",-1608172596),"Figwheel: loaded these files");
var inst_42709 = (function (){var all_files = inst_42697;
var files_SINGLEQUOTE_ = inst_42698;
var res_SINGLEQUOTE_ = inst_42701;
var res = inst_42702;
var files_not_loaded = inst_42704;
return ((function (all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,files_not_loaded,inst_42704,inst_42697,inst_42698,inst_42702,inst_42701,inst_42707,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (p__42708){
var map__42772 = p__42708;
var map__42772__$1 = ((cljs.core.seq_QMARK_.call(null,map__42772))?cljs.core.apply.call(null,cljs.core.hash_map,map__42772):map__42772);
var namespace = cljs.core.get.call(null,map__42772__$1,new cljs.core.Keyword(null,"namespace","namespace",-377510372));
var file = cljs.core.get.call(null,map__42772__$1,new cljs.core.Keyword(null,"file","file",-1269645878));
if(cljs.core.truth_(namespace)){
return figwheel.client.file_reloading.ns_to_js_file.call(null,namespace);
} else {
return file;
}
});
;})(all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,files_not_loaded,inst_42704,inst_42697,inst_42698,inst_42702,inst_42701,inst_42707,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var inst_42710 = cljs.core.map.call(null,inst_42709,inst_42702);
var inst_42711 = cljs.core.pr_str.call(null,inst_42710);
var inst_42712 = figwheel.client.utils.log.call(null,inst_42711);
var inst_42713 = (function (){var all_files = inst_42697;
var files_SINGLEQUOTE_ = inst_42698;
var res_SINGLEQUOTE_ = inst_42701;
var res = inst_42702;
var files_not_loaded = inst_42704;
return ((function (all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,files_not_loaded,inst_42704,inst_42697,inst_42698,inst_42702,inst_42701,inst_42707,inst_42709,inst_42710,inst_42711,inst_42712,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (){
figwheel.client.file_reloading.on_jsload_custom_event.call(null,res);

return cljs.core.apply.call(null,on_jsload,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [res], null));
});
;})(all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,files_not_loaded,inst_42704,inst_42697,inst_42698,inst_42702,inst_42701,inst_42707,inst_42709,inst_42710,inst_42711,inst_42712,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var inst_42714 = setTimeout(inst_42713,(10));
var state_42765__$1 = (function (){var statearr_42773 = state_42765;
(statearr_42773[(17)] = inst_42712);

(statearr_42773[(18)] = inst_42707);

return statearr_42773;
})();
var statearr_42774_42841 = state_42765__$1;
(statearr_42774_42841[(2)] = inst_42714);

(statearr_42774_42841[(1)] = (22));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (27))){
var inst_42724 = (state_42765[(19)]);
var state_42765__$1 = state_42765;
var statearr_42775_42842 = state_42765__$1;
(statearr_42775_42842[(2)] = inst_42724);

(statearr_42775_42842[(1)] = (28));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (1))){
var inst_42644 = (state_42765[(20)]);
var inst_42641 = before_jsload.call(null,files);
var inst_42642 = figwheel.client.file_reloading.before_jsload_custom_event.call(null,files);
var inst_42643 = (function (){return ((function (inst_42644,inst_42641,inst_42642,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (p1__42432_SHARP_){
return new cljs.core.Keyword(null,"eval-body","eval-body",-907279883).cljs$core$IFn$_invoke$arity$1(p1__42432_SHARP_);
});
;})(inst_42644,inst_42641,inst_42642,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var inst_42644__$1 = cljs.core.filter.call(null,inst_42643,files);
var inst_42645 = cljs.core.not_empty.call(null,inst_42644__$1);
var state_42765__$1 = (function (){var statearr_42776 = state_42765;
(statearr_42776[(21)] = inst_42641);

(statearr_42776[(20)] = inst_42644__$1);

(statearr_42776[(22)] = inst_42642);

return statearr_42776;
})();
if(cljs.core.truth_(inst_42645)){
var statearr_42777_42843 = state_42765__$1;
(statearr_42777_42843[(1)] = (2));

} else {
var statearr_42778_42844 = state_42765__$1;
(statearr_42778_42844[(1)] = (3));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (24))){
var state_42765__$1 = state_42765;
var statearr_42779_42845 = state_42765__$1;
(statearr_42779_42845[(2)] = null);

(statearr_42779_42845[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (4))){
var inst_42689 = (state_42765[(2)]);
var inst_42690 = (function (){return ((function (inst_42689,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (p1__42433_SHARP_){
var and__16057__auto__ = new cljs.core.Keyword(null,"namespace","namespace",-377510372).cljs$core$IFn$_invoke$arity$1(p1__42433_SHARP_);
if(cljs.core.truth_(and__16057__auto__)){
return cljs.core.not.call(null,new cljs.core.Keyword(null,"eval-body","eval-body",-907279883).cljs$core$IFn$_invoke$arity$1(p1__42433_SHARP_));
} else {
return and__16057__auto__;
}
});
;})(inst_42689,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var inst_42691 = cljs.core.filter.call(null,inst_42690,files);
var state_42765__$1 = (function (){var statearr_42780 = state_42765;
(statearr_42780[(23)] = inst_42691);

(statearr_42780[(24)] = inst_42689);

return statearr_42780;
})();
if(cljs.core.truth_(load_unchanged_files)){
var statearr_42781_42846 = state_42765__$1;
(statearr_42781_42846[(1)] = (16));

} else {
var statearr_42782_42847 = state_42765__$1;
(statearr_42782_42847[(1)] = (17));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (15))){
var inst_42679 = (state_42765[(2)]);
var state_42765__$1 = state_42765;
var statearr_42783_42848 = state_42765__$1;
(statearr_42783_42848[(2)] = inst_42679);

(statearr_42783_42848[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (21))){
var state_42765__$1 = state_42765;
var statearr_42784_42849 = state_42765__$1;
(statearr_42784_42849[(2)] = null);

(statearr_42784_42849[(1)] = (22));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (31))){
var inst_42732 = (state_42765[(25)]);
var inst_42742 = (state_42765[(2)]);
var inst_42743 = cljs.core.not_empty.call(null,inst_42732);
var state_42765__$1 = (function (){var statearr_42785 = state_42765;
(statearr_42785[(26)] = inst_42742);

return statearr_42785;
})();
if(cljs.core.truth_(inst_42743)){
var statearr_42786_42850 = state_42765__$1;
(statearr_42786_42850[(1)] = (32));

} else {
var statearr_42787_42851 = state_42765__$1;
(statearr_42787_42851[(1)] = (33));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (32))){
var inst_42732 = (state_42765[(25)]);
var inst_42745 = cljs.core.map.call(null,new cljs.core.Keyword(null,"file","file",-1269645878),inst_42732);
var inst_42746 = cljs.core.pr_str.call(null,inst_42745);
var inst_42747 = [cljs.core.str("file didn't change: "),cljs.core.str(inst_42746)].join('');
var inst_42748 = figwheel.client.utils.log.call(null,inst_42747);
var state_42765__$1 = state_42765;
var statearr_42788_42852 = state_42765__$1;
(statearr_42788_42852[(2)] = inst_42748);

(statearr_42788_42852[(1)] = (34));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (33))){
var state_42765__$1 = state_42765;
var statearr_42789_42853 = state_42765__$1;
(statearr_42789_42853[(2)] = null);

(statearr_42789_42853[(1)] = (34));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (13))){
var inst_42665 = (state_42765[(27)]);
var inst_42669 = cljs.core.chunk_first.call(null,inst_42665);
var inst_42670 = cljs.core.chunk_rest.call(null,inst_42665);
var inst_42671 = cljs.core.count.call(null,inst_42669);
var inst_42652 = inst_42670;
var inst_42653 = inst_42669;
var inst_42654 = inst_42671;
var inst_42655 = (0);
var state_42765__$1 = (function (){var statearr_42790 = state_42765;
(statearr_42790[(7)] = inst_42652);

(statearr_42790[(8)] = inst_42654);

(statearr_42790[(9)] = inst_42655);

(statearr_42790[(10)] = inst_42653);

return statearr_42790;
})();
var statearr_42791_42854 = state_42765__$1;
(statearr_42791_42854[(2)] = null);

(statearr_42791_42854[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (22))){
var inst_42704 = (state_42765[(12)]);
var inst_42717 = (state_42765[(2)]);
var inst_42718 = cljs.core.not_empty.call(null,inst_42704);
var state_42765__$1 = (function (){var statearr_42792 = state_42765;
(statearr_42792[(28)] = inst_42717);

return statearr_42792;
})();
if(cljs.core.truth_(inst_42718)){
var statearr_42793_42855 = state_42765__$1;
(statearr_42793_42855[(1)] = (23));

} else {
var statearr_42794_42856 = state_42765__$1;
(statearr_42794_42856[(1)] = (24));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (36))){
var state_42765__$1 = state_42765;
var statearr_42795_42857 = state_42765__$1;
(statearr_42795_42857[(2)] = null);

(statearr_42795_42857[(1)] = (37));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (29))){
var inst_42731 = (state_42765[(29)]);
var inst_42736 = cljs.core.map.call(null,new cljs.core.Keyword(null,"file","file",-1269645878),inst_42731);
var inst_42737 = cljs.core.pr_str.call(null,inst_42736);
var inst_42738 = [cljs.core.str("figwheel-no-load meta-data: "),cljs.core.str(inst_42737)].join('');
var inst_42739 = figwheel.client.utils.log.call(null,inst_42738);
var state_42765__$1 = state_42765;
var statearr_42796_42858 = state_42765__$1;
(statearr_42796_42858[(2)] = inst_42739);

(statearr_42796_42858[(1)] = (31));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (6))){
var inst_42686 = (state_42765[(2)]);
var state_42765__$1 = state_42765;
var statearr_42797_42859 = state_42765__$1;
(statearr_42797_42859[(2)] = inst_42686);

(statearr_42797_42859[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (28))){
var inst_42731 = (state_42765[(29)]);
var inst_42730 = (state_42765[(2)]);
var inst_42731__$1 = cljs.core.get.call(null,inst_42730,new cljs.core.Keyword(null,"figwheel-no-load","figwheel-no-load",-555840179));
var inst_42732 = cljs.core.get.call(null,inst_42730,new cljs.core.Keyword(null,"file-changed-on-disk","file-changed-on-disk",1086171932));
var inst_42733 = cljs.core.get.call(null,inst_42730,new cljs.core.Keyword(null,"not-required","not-required",-950359114));
var inst_42734 = cljs.core.not_empty.call(null,inst_42731__$1);
var state_42765__$1 = (function (){var statearr_42798 = state_42765;
(statearr_42798[(30)] = inst_42733);

(statearr_42798[(25)] = inst_42732);

(statearr_42798[(29)] = inst_42731__$1);

return statearr_42798;
})();
if(cljs.core.truth_(inst_42734)){
var statearr_42799_42860 = state_42765__$1;
(statearr_42799_42860[(1)] = (29));

} else {
var statearr_42800_42861 = state_42765__$1;
(statearr_42800_42861[(1)] = (30));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (25))){
var inst_42763 = (state_42765[(2)]);
var state_42765__$1 = state_42765;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_42765__$1,inst_42763);
} else {
if((state_val_42766 === (34))){
var inst_42733 = (state_42765[(30)]);
var inst_42751 = (state_42765[(2)]);
var inst_42752 = cljs.core.not_empty.call(null,inst_42733);
var state_42765__$1 = (function (){var statearr_42801 = state_42765;
(statearr_42801[(31)] = inst_42751);

return statearr_42801;
})();
if(cljs.core.truth_(inst_42752)){
var statearr_42802_42862 = state_42765__$1;
(statearr_42802_42862[(1)] = (35));

} else {
var statearr_42803_42863 = state_42765__$1;
(statearr_42803_42863[(1)] = (36));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (17))){
var inst_42691 = (state_42765[(23)]);
var state_42765__$1 = state_42765;
var statearr_42804_42864 = state_42765__$1;
(statearr_42804_42864[(2)] = inst_42691);

(statearr_42804_42864[(1)] = (18));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (3))){
var state_42765__$1 = state_42765;
var statearr_42805_42865 = state_42765__$1;
(statearr_42805_42865[(2)] = null);

(statearr_42805_42865[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (12))){
var inst_42682 = (state_42765[(2)]);
var state_42765__$1 = state_42765;
var statearr_42806_42866 = state_42765__$1;
(statearr_42806_42866[(2)] = inst_42682);

(statearr_42806_42866[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (2))){
var inst_42644 = (state_42765[(20)]);
var inst_42651 = cljs.core.seq.call(null,inst_42644);
var inst_42652 = inst_42651;
var inst_42653 = null;
var inst_42654 = (0);
var inst_42655 = (0);
var state_42765__$1 = (function (){var statearr_42807 = state_42765;
(statearr_42807[(7)] = inst_42652);

(statearr_42807[(8)] = inst_42654);

(statearr_42807[(9)] = inst_42655);

(statearr_42807[(10)] = inst_42653);

return statearr_42807;
})();
var statearr_42808_42867 = state_42765__$1;
(statearr_42808_42867[(2)] = null);

(statearr_42808_42867[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (23))){
var inst_42704 = (state_42765[(12)]);
var inst_42697 = (state_42765[(13)]);
var inst_42698 = (state_42765[(14)]);
var inst_42724 = (state_42765[(19)]);
var inst_42702 = (state_42765[(15)]);
var inst_42701 = (state_42765[(16)]);
var inst_42720 = figwheel.client.utils.log.call(null,new cljs.core.Keyword(null,"debug","debug",-1608172596),"Figwheel: NOT loading these files ");
var inst_42723 = (function (){var all_files = inst_42697;
var files_SINGLEQUOTE_ = inst_42698;
var res_SINGLEQUOTE_ = inst_42701;
var res = inst_42702;
var files_not_loaded = inst_42704;
return ((function (all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,files_not_loaded,inst_42704,inst_42697,inst_42698,inst_42724,inst_42702,inst_42701,inst_42720,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (p__42722){
var map__42809 = p__42722;
var map__42809__$1 = ((cljs.core.seq_QMARK_.call(null,map__42809))?cljs.core.apply.call(null,cljs.core.hash_map,map__42809):map__42809);
var meta_data = cljs.core.get.call(null,map__42809__$1,new cljs.core.Keyword(null,"meta-data","meta-data",-1613399157));
if((meta_data == null)){
return new cljs.core.Keyword(null,"not-required","not-required",-950359114);
} else {
if(cljs.core.contains_QMARK_.call(null,meta_data,new cljs.core.Keyword(null,"figwheel-no-load","figwheel-no-load",-555840179))){
return new cljs.core.Keyword(null,"figwheel-no-load","figwheel-no-load",-555840179);
} else {
if((cljs.core.contains_QMARK_.call(null,meta_data,new cljs.core.Keyword(null,"file-changed-on-disk","file-changed-on-disk",1086171932))) && (cljs.core.not.call(null,new cljs.core.Keyword(null,"file-changed-on-disk","file-changed-on-disk",1086171932).cljs$core$IFn$_invoke$arity$1(meta_data)))){
return new cljs.core.Keyword(null,"file-changed-on-disk","file-changed-on-disk",1086171932);
} else {
return new cljs.core.Keyword(null,"not-required","not-required",-950359114);

}
}
}
});
;})(all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,files_not_loaded,inst_42704,inst_42697,inst_42698,inst_42724,inst_42702,inst_42701,inst_42720,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var inst_42724__$1 = cljs.core.group_by.call(null,inst_42723,inst_42704);
var inst_42725 = cljs.core.seq_QMARK_.call(null,inst_42724__$1);
var state_42765__$1 = (function (){var statearr_42810 = state_42765;
(statearr_42810[(32)] = inst_42720);

(statearr_42810[(19)] = inst_42724__$1);

return statearr_42810;
})();
if(inst_42725){
var statearr_42811_42868 = state_42765__$1;
(statearr_42811_42868[(1)] = (26));

} else {
var statearr_42812_42869 = state_42765__$1;
(statearr_42812_42869[(1)] = (27));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (35))){
var inst_42733 = (state_42765[(30)]);
var inst_42754 = cljs.core.map.call(null,new cljs.core.Keyword(null,"file","file",-1269645878),inst_42733);
var inst_42755 = cljs.core.pr_str.call(null,inst_42754);
var inst_42756 = [cljs.core.str("not required: "),cljs.core.str(inst_42755)].join('');
var inst_42757 = figwheel.client.utils.log.call(null,inst_42756);
var state_42765__$1 = state_42765;
var statearr_42813_42870 = state_42765__$1;
(statearr_42813_42870[(2)] = inst_42757);

(statearr_42813_42870[(1)] = (37));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (19))){
var inst_42697 = (state_42765[(13)]);
var inst_42698 = (state_42765[(14)]);
var inst_42702 = (state_42765[(15)]);
var inst_42701 = (state_42765[(16)]);
var inst_42701__$1 = (state_42765[(2)]);
var inst_42702__$1 = cljs.core.filter.call(null,new cljs.core.Keyword(null,"loaded-file","loaded-file",-168399375),inst_42701__$1);
var inst_42703 = (function (){var all_files = inst_42697;
var files_SINGLEQUOTE_ = inst_42698;
var res_SINGLEQUOTE_ = inst_42701__$1;
var res = inst_42702__$1;
return ((function (all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,inst_42697,inst_42698,inst_42702,inst_42701,inst_42701__$1,inst_42702__$1,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (p1__42435_SHARP_){
return cljs.core.not.call(null,new cljs.core.Keyword(null,"loaded-file","loaded-file",-168399375).cljs$core$IFn$_invoke$arity$1(p1__42435_SHARP_));
});
;})(all_files,files_SINGLEQUOTE_,res_SINGLEQUOTE_,res,inst_42697,inst_42698,inst_42702,inst_42701,inst_42701__$1,inst_42702__$1,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var inst_42704 = cljs.core.filter.call(null,inst_42703,inst_42701__$1);
var inst_42705 = cljs.core.not_empty.call(null,inst_42702__$1);
var state_42765__$1 = (function (){var statearr_42814 = state_42765;
(statearr_42814[(12)] = inst_42704);

(statearr_42814[(15)] = inst_42702__$1);

(statearr_42814[(16)] = inst_42701__$1);

return statearr_42814;
})();
if(cljs.core.truth_(inst_42705)){
var statearr_42815_42871 = state_42765__$1;
(statearr_42815_42871[(1)] = (20));

} else {
var statearr_42816_42872 = state_42765__$1;
(statearr_42816_42872[(1)] = (21));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (11))){
var state_42765__$1 = state_42765;
var statearr_42817_42873 = state_42765__$1;
(statearr_42817_42873[(2)] = null);

(statearr_42817_42873[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (9))){
var inst_42684 = (state_42765[(2)]);
var state_42765__$1 = state_42765;
var statearr_42818_42874 = state_42765__$1;
(statearr_42818_42874[(2)] = inst_42684);

(statearr_42818_42874[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (5))){
var inst_42654 = (state_42765[(8)]);
var inst_42655 = (state_42765[(9)]);
var inst_42657 = (inst_42655 < inst_42654);
var inst_42658 = inst_42657;
var state_42765__$1 = state_42765;
if(cljs.core.truth_(inst_42658)){
var statearr_42819_42875 = state_42765__$1;
(statearr_42819_42875[(1)] = (7));

} else {
var statearr_42820_42876 = state_42765__$1;
(statearr_42820_42876[(1)] = (8));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (14))){
var inst_42665 = (state_42765[(27)]);
var inst_42674 = cljs.core.first.call(null,inst_42665);
var inst_42675 = figwheel.client.file_reloading.eval_body.call(null,inst_42674,opts);
var inst_42676 = cljs.core.next.call(null,inst_42665);
var inst_42652 = inst_42676;
var inst_42653 = null;
var inst_42654 = (0);
var inst_42655 = (0);
var state_42765__$1 = (function (){var statearr_42821 = state_42765;
(statearr_42821[(7)] = inst_42652);

(statearr_42821[(33)] = inst_42675);

(statearr_42821[(8)] = inst_42654);

(statearr_42821[(9)] = inst_42655);

(statearr_42821[(10)] = inst_42653);

return statearr_42821;
})();
var statearr_42822_42877 = state_42765__$1;
(statearr_42822_42877[(2)] = null);

(statearr_42822_42877[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (26))){
var inst_42724 = (state_42765[(19)]);
var inst_42727 = cljs.core.apply.call(null,cljs.core.hash_map,inst_42724);
var state_42765__$1 = state_42765;
var statearr_42823_42878 = state_42765__$1;
(statearr_42823_42878[(2)] = inst_42727);

(statearr_42823_42878[(1)] = (28));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (16))){
var inst_42691 = (state_42765[(23)]);
var inst_42693 = (function (){var all_files = inst_42691;
return ((function (all_files,inst_42691,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function (p1__42434_SHARP_){
return cljs.core.update_in.call(null,p1__42434_SHARP_,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"meta-data","meta-data",-1613399157)], null),cljs.core.dissoc,new cljs.core.Keyword(null,"file-changed-on-disk","file-changed-on-disk",1086171932));
});
;})(all_files,inst_42691,state_val_42766,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var inst_42694 = cljs.core.map.call(null,inst_42693,inst_42691);
var state_42765__$1 = state_42765;
var statearr_42824_42879 = state_42765__$1;
(statearr_42824_42879[(2)] = inst_42694);

(statearr_42824_42879[(1)] = (18));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (30))){
var state_42765__$1 = state_42765;
var statearr_42825_42880 = state_42765__$1;
(statearr_42825_42880[(2)] = null);

(statearr_42825_42880[(1)] = (31));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (10))){
var inst_42665 = (state_42765[(27)]);
var inst_42667 = cljs.core.chunked_seq_QMARK_.call(null,inst_42665);
var state_42765__$1 = state_42765;
if(inst_42667){
var statearr_42826_42881 = state_42765__$1;
(statearr_42826_42881[(1)] = (13));

} else {
var statearr_42827_42882 = state_42765__$1;
(statearr_42827_42882[(1)] = (14));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (18))){
var inst_42697 = (state_42765[(13)]);
var inst_42698 = (state_42765[(14)]);
var inst_42697__$1 = (state_42765[(2)]);
var inst_42698__$1 = figwheel.client.file_reloading.add_request_urls.call(null,opts,inst_42697__$1);
var inst_42699 = figwheel.client.file_reloading.load_all_js_files.call(null,inst_42698__$1);
var state_42765__$1 = (function (){var statearr_42828 = state_42765;
(statearr_42828[(13)] = inst_42697__$1);

(statearr_42828[(14)] = inst_42698__$1);

return statearr_42828;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42765__$1,(19),inst_42699);
} else {
if((state_val_42766 === (37))){
var inst_42760 = (state_42765[(2)]);
var state_42765__$1 = state_42765;
var statearr_42829_42883 = state_42765__$1;
(statearr_42829_42883[(2)] = inst_42760);

(statearr_42829_42883[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_42766 === (8))){
var inst_42652 = (state_42765[(7)]);
var inst_42665 = (state_42765[(27)]);
var inst_42665__$1 = cljs.core.seq.call(null,inst_42652);
var state_42765__$1 = (function (){var statearr_42830 = state_42765;
(statearr_42830[(27)] = inst_42665__$1);

return statearr_42830;
})();
if(inst_42665__$1){
var statearr_42831_42884 = state_42765__$1;
(statearr_42831_42884[(1)] = (10));

} else {
var statearr_42832_42885 = state_42765__$1;
(statearr_42832_42885[(1)] = (11));

}

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
}
}
}
});})(c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
;
return ((function (switch__23571__auto__,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files){
return (function() {
var figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto__ = null;
var figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto____0 = (function (){
var statearr_42836 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_42836[(0)] = figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto__);

(statearr_42836[(1)] = (1));

return statearr_42836;
});
var figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto____1 = (function (state_42765){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_42765);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e42837){if((e42837 instanceof Object)){
var ex__23575__auto__ = e42837;
var statearr_42838_42886 = state_42765;
(statearr_42838_42886[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_42765);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e42837;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__42887 = state_42765;
state_42765 = G__42887;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto__ = function(state_42765){
switch(arguments.length){
case 0:
return figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto____1.call(this,state_42765);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto____0;
figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto____1;
return figwheel$client$file_reloading$reload_js_files_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
})();
var state__23635__auto__ = (function (){var statearr_42839 = f__23634__auto__.call(null);
(statearr_42839[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_42839;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__,map__42639,map__42639__$1,opts,before_jsload,on_jsload,load_unchanged_files,map__42640,map__42640__$1,msg,files))
);

return c__23633__auto__;
});
figwheel.client.file_reloading.current_links = (function figwheel$client$file_reloading$current_links(){
return Array.prototype.slice.call(document.getElementsByTagName("link"));
});
figwheel.client.file_reloading.truncate_url = (function figwheel$client$file_reloading$truncate_url(url){
return clojure.string.replace_first.call(null,clojure.string.replace_first.call(null,clojure.string.replace_first.call(null,clojure.string.replace_first.call(null,cljs.core.first.call(null,clojure.string.split.call(null,url,/\?/)),[cljs.core.str(location.protocol),cljs.core.str("//")].join(''),""),".*://",""),/^\/\//,""),/[^\\/]*/,"");
});
figwheel.client.file_reloading.matches_file_QMARK_ = (function figwheel$client$file_reloading$matches_file_QMARK_(p__42890,link){
var map__42892 = p__42890;
var map__42892__$1 = ((cljs.core.seq_QMARK_.call(null,map__42892))?cljs.core.apply.call(null,cljs.core.hash_map,map__42892):map__42892);
var file = cljs.core.get.call(null,map__42892__$1,new cljs.core.Keyword(null,"file","file",-1269645878));
var temp__4425__auto__ = link.href;
if(cljs.core.truth_(temp__4425__auto__)){
var link_href = temp__4425__auto__;
var match = clojure.string.join.call(null,"/",cljs.core.take_while.call(null,cljs.core.identity,cljs.core.map.call(null,((function (link_href,temp__4425__auto__,map__42892,map__42892__$1,file){
return (function (p1__42888_SHARP_,p2__42889_SHARP_){
if(cljs.core._EQ_.call(null,p1__42888_SHARP_,p2__42889_SHARP_)){
return p1__42888_SHARP_;
} else {
return false;
}
});})(link_href,temp__4425__auto__,map__42892,map__42892__$1,file))
,cljs.core.reverse.call(null,clojure.string.split.call(null,file,"/")),cljs.core.reverse.call(null,clojure.string.split.call(null,figwheel.client.file_reloading.truncate_url.call(null,link_href),"/")))));
var match_length = cljs.core.count.call(null,match);
var file_name_length = cljs.core.count.call(null,cljs.core.last.call(null,clojure.string.split.call(null,file,"/")));
if((match_length >= file_name_length)){
return new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"link","link",-1769163468),link,new cljs.core.Keyword(null,"link-href","link-href",-250644450),link_href,new cljs.core.Keyword(null,"match-length","match-length",1101537310),match_length,new cljs.core.Keyword(null,"current-url-length","current-url-length",380404083),cljs.core.count.call(null,figwheel.client.file_reloading.truncate_url.call(null,link_href))], null);
} else {
return null;
}
} else {
return null;
}
});
figwheel.client.file_reloading.get_correct_link = (function figwheel$client$file_reloading$get_correct_link(f_data){
var temp__4425__auto__ = cljs.core.first.call(null,cljs.core.sort_by.call(null,(function (p__42896){
var map__42897 = p__42896;
var map__42897__$1 = ((cljs.core.seq_QMARK_.call(null,map__42897))?cljs.core.apply.call(null,cljs.core.hash_map,map__42897):map__42897);
var match_length = cljs.core.get.call(null,map__42897__$1,new cljs.core.Keyword(null,"match-length","match-length",1101537310));
var current_url_length = cljs.core.get.call(null,map__42897__$1,new cljs.core.Keyword(null,"current-url-length","current-url-length",380404083));
return (current_url_length - match_length);
}),cljs.core.keep.call(null,(function (p1__42893_SHARP_){
return figwheel.client.file_reloading.matches_file_QMARK_.call(null,f_data,p1__42893_SHARP_);
}),figwheel.client.file_reloading.current_links.call(null))));
if(cljs.core.truth_(temp__4425__auto__)){
var res = temp__4425__auto__;
return new cljs.core.Keyword(null,"link","link",-1769163468).cljs$core$IFn$_invoke$arity$1(res);
} else {
return null;
}
});
figwheel.client.file_reloading.clone_link = (function figwheel$client$file_reloading$clone_link(link,url){
var clone = document.createElement("link");
clone.rel = "stylesheet";

clone.media = link.media;

clone.disabled = link.disabled;

clone.href = figwheel.client.file_reloading.add_cache_buster.call(null,url);

return clone;
});
figwheel.client.file_reloading.create_link = (function figwheel$client$file_reloading$create_link(url){
var link = document.createElement("link");
link.rel = "stylesheet";

link.href = figwheel.client.file_reloading.add_cache_buster.call(null,url);

return link;
});
figwheel.client.file_reloading.add_link_to_doc = (function figwheel$client$file_reloading$add_link_to_doc(){
var G__42899 = arguments.length;
switch (G__42899) {
case 1:
return figwheel.client.file_reloading.add_link_to_doc.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return figwheel.client.file_reloading.add_link_to_doc.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

figwheel.client.file_reloading.add_link_to_doc.cljs$core$IFn$_invoke$arity$1 = (function (new_link){
return (document.getElementsByTagName("head")[(0)]).appendChild(new_link);
});

figwheel.client.file_reloading.add_link_to_doc.cljs$core$IFn$_invoke$arity$2 = (function (orig_link,klone){
var parent = orig_link.parentNode;
if(cljs.core._EQ_.call(null,orig_link,parent.lastChild)){
parent.appendChild(klone);
} else {
parent.insertBefore(klone,orig_link.nextSibling);
}

return setTimeout(((function (parent){
return (function (){
return parent.removeChild(orig_link);
});})(parent))
,(300));
});

figwheel.client.file_reloading.add_link_to_doc.cljs$lang$maxFixedArity = 2;
figwheel.client.file_reloading.reload_css_file = (function figwheel$client$file_reloading$reload_css_file(p__42901){
var map__42903 = p__42901;
var map__42903__$1 = ((cljs.core.seq_QMARK_.call(null,map__42903))?cljs.core.apply.call(null,cljs.core.hash_map,map__42903):map__42903);
var f_data = map__42903__$1;
var file = cljs.core.get.call(null,map__42903__$1,new cljs.core.Keyword(null,"file","file",-1269645878));
var request_url = cljs.core.get.call(null,map__42903__$1,new cljs.core.Keyword(null,"request-url","request-url",2100346596));
var temp__4423__auto__ = figwheel.client.file_reloading.get_correct_link.call(null,f_data);
if(cljs.core.truth_(temp__4423__auto__)){
var link = temp__4423__auto__;
return figwheel.client.file_reloading.add_link_to_doc.call(null,link,figwheel.client.file_reloading.clone_link.call(null,link,link.href));
} else {
return figwheel.client.file_reloading.add_link_to_doc.call(null,figwheel.client.file_reloading.create_link.call(null,(function (){var or__16069__auto__ = request_url;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return file;
}
})()));
}
});
figwheel.client.file_reloading.reload_css_files = (function figwheel$client$file_reloading$reload_css_files(p__42904,files_msg){
var map__42926 = p__42904;
var map__42926__$1 = ((cljs.core.seq_QMARK_.call(null,map__42926))?cljs.core.apply.call(null,cljs.core.hash_map,map__42926):map__42926);
var opts = map__42926__$1;
var on_cssload = cljs.core.get.call(null,map__42926__$1,new cljs.core.Keyword(null,"on-cssload","on-cssload",1825432318));
if(cljs.core.truth_(figwheel.client.utils.html_env_QMARK_.call(null))){
var seq__42927_42947 = cljs.core.seq.call(null,figwheel.client.file_reloading.add_request_urls.call(null,opts,new cljs.core.Keyword(null,"files","files",-472457450).cljs$core$IFn$_invoke$arity$1(files_msg)));
var chunk__42928_42948 = null;
var count__42929_42949 = (0);
var i__42930_42950 = (0);
while(true){
if((i__42930_42950 < count__42929_42949)){
var f_42951 = cljs.core._nth.call(null,chunk__42928_42948,i__42930_42950);
figwheel.client.file_reloading.reload_css_file.call(null,f_42951);

var G__42952 = seq__42927_42947;
var G__42953 = chunk__42928_42948;
var G__42954 = count__42929_42949;
var G__42955 = (i__42930_42950 + (1));
seq__42927_42947 = G__42952;
chunk__42928_42948 = G__42953;
count__42929_42949 = G__42954;
i__42930_42950 = G__42955;
continue;
} else {
var temp__4425__auto___42956 = cljs.core.seq.call(null,seq__42927_42947);
if(temp__4425__auto___42956){
var seq__42927_42957__$1 = temp__4425__auto___42956;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__42927_42957__$1)){
var c__16854__auto___42958 = cljs.core.chunk_first.call(null,seq__42927_42957__$1);
var G__42959 = cljs.core.chunk_rest.call(null,seq__42927_42957__$1);
var G__42960 = c__16854__auto___42958;
var G__42961 = cljs.core.count.call(null,c__16854__auto___42958);
var G__42962 = (0);
seq__42927_42947 = G__42959;
chunk__42928_42948 = G__42960;
count__42929_42949 = G__42961;
i__42930_42950 = G__42962;
continue;
} else {
var f_42963 = cljs.core.first.call(null,seq__42927_42957__$1);
figwheel.client.file_reloading.reload_css_file.call(null,f_42963);

var G__42964 = cljs.core.next.call(null,seq__42927_42957__$1);
var G__42965 = null;
var G__42966 = (0);
var G__42967 = (0);
seq__42927_42947 = G__42964;
chunk__42928_42948 = G__42965;
count__42929_42949 = G__42966;
i__42930_42950 = G__42967;
continue;
}
} else {
}
}
break;
}

var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__,map__42926,map__42926__$1,opts,on_cssload){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__,map__42926,map__42926__$1,opts,on_cssload){
return (function (state_42937){
var state_val_42938 = (state_42937[(1)]);
if((state_val_42938 === (1))){
var inst_42931 = cljs.core.async.timeout.call(null,(100));
var state_42937__$1 = state_42937;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_42937__$1,(2),inst_42931);
} else {
if((state_val_42938 === (2))){
var inst_42933 = (state_42937[(2)]);
var inst_42934 = new cljs.core.Keyword(null,"files","files",-472457450).cljs$core$IFn$_invoke$arity$1(files_msg);
var inst_42935 = on_cssload.call(null,inst_42934);
var state_42937__$1 = (function (){var statearr_42939 = state_42937;
(statearr_42939[(7)] = inst_42933);

return statearr_42939;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_42937__$1,inst_42935);
} else {
return null;
}
}
});})(c__23633__auto__,map__42926,map__42926__$1,opts,on_cssload))
;
return ((function (switch__23571__auto__,c__23633__auto__,map__42926,map__42926__$1,opts,on_cssload){
return (function() {
var figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto__ = null;
var figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto____0 = (function (){
var statearr_42943 = [null,null,null,null,null,null,null,null];
(statearr_42943[(0)] = figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto__);

(statearr_42943[(1)] = (1));

return statearr_42943;
});
var figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto____1 = (function (state_42937){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_42937);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e42944){if((e42944 instanceof Object)){
var ex__23575__auto__ = e42944;
var statearr_42945_42968 = state_42937;
(statearr_42945_42968[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_42937);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e42944;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__42969 = state_42937;
state_42937 = G__42969;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto__ = function(state_42937){
switch(arguments.length){
case 0:
return figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto____0.call(this);
case 1:
return figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto____1.call(this,state_42937);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto____0;
figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto____1;
return figwheel$client$file_reloading$reload_css_files_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__,map__42926,map__42926__$1,opts,on_cssload))
})();
var state__23635__auto__ = (function (){var statearr_42946 = f__23634__auto__.call(null);
(statearr_42946[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_42946;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__,map__42926,map__42926__$1,opts,on_cssload))
);

return c__23633__auto__;
} else {
return null;
}
});

//# sourceMappingURL=file_reloading.js.map?rel=1439206059865
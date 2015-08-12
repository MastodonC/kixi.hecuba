// Compiled by ClojureScript 0.0-3297 {}
goog.provide('cljs_http.core');
goog.require('cljs.core');
goog.require('goog.net.ErrorCode');
goog.require('goog.net.EventType');
goog.require('cljs.core.async');
goog.require('cljs_http.util');
goog.require('goog.net.Jsonp');
goog.require('clojure.string');
goog.require('goog.net.XhrIo');
cljs_http.core.pending_requests = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
/**
 * Attempt to close the given channel and abort the pending HTTP request
 * with which it is associated.
 */
cljs_http.core.abort_BANG_ = (function cljs_http$core$abort_BANG_(channel){
var temp__4425__auto__ = cljs.core.deref.call(null,cljs_http.core.pending_requests).call(null,channel);
if(cljs.core.truth_(temp__4425__auto__)){
var req = temp__4425__auto__;
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

cljs.core.async.close_BANG_.call(null,channel);

if(cljs.core.truth_(req.hasOwnProperty("abort"))){
return req.abort();
} else {
return new cljs.core.Keyword(null,"jsonp","jsonp",226119588).cljs$core$IFn$_invoke$arity$1(req).cancel(new cljs.core.Keyword(null,"request","request",1772954723).cljs$core$IFn$_invoke$arity$1(req));
}
} else {
return null;
}
});
cljs_http.core.aborted_QMARK_ = (function cljs_http$core$aborted_QMARK_(xhr){
return cljs.core._EQ_.call(null,xhr.getLastErrorCode(),goog.net.ErrorCode.ABORT);
});
/**
 * Takes an XhrIo object and applies the default-headers to it.
 */
cljs_http.core.apply_default_headers_BANG_ = (function cljs_http$core$apply_default_headers_BANG_(xhr,headers){
var seq__36729 = cljs.core.seq.call(null,cljs.core.map.call(null,cljs_http.util.camelize,cljs.core.keys.call(null,headers)));
var chunk__36734 = null;
var count__36735 = (0);
var i__36736 = (0);
while(true){
if((i__36736 < count__36735)){
var h_name = cljs.core._nth.call(null,chunk__36734,i__36736);
var seq__36737_36741 = cljs.core.seq.call(null,cljs.core.vals.call(null,headers));
var chunk__36738_36742 = null;
var count__36739_36743 = (0);
var i__36740_36744 = (0);
while(true){
if((i__36740_36744 < count__36739_36743)){
var h_val_36745 = cljs.core._nth.call(null,chunk__36738_36742,i__36740_36744);
xhr.headers.set(h_name,h_val_36745);

var G__36746 = seq__36737_36741;
var G__36747 = chunk__36738_36742;
var G__36748 = count__36739_36743;
var G__36749 = (i__36740_36744 + (1));
seq__36737_36741 = G__36746;
chunk__36738_36742 = G__36747;
count__36739_36743 = G__36748;
i__36740_36744 = G__36749;
continue;
} else {
var temp__4425__auto___36750 = cljs.core.seq.call(null,seq__36737_36741);
if(temp__4425__auto___36750){
var seq__36737_36751__$1 = temp__4425__auto___36750;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__36737_36751__$1)){
var c__16854__auto___36752 = cljs.core.chunk_first.call(null,seq__36737_36751__$1);
var G__36753 = cljs.core.chunk_rest.call(null,seq__36737_36751__$1);
var G__36754 = c__16854__auto___36752;
var G__36755 = cljs.core.count.call(null,c__16854__auto___36752);
var G__36756 = (0);
seq__36737_36741 = G__36753;
chunk__36738_36742 = G__36754;
count__36739_36743 = G__36755;
i__36740_36744 = G__36756;
continue;
} else {
var h_val_36757 = cljs.core.first.call(null,seq__36737_36751__$1);
xhr.headers.set(h_name,h_val_36757);

var G__36758 = cljs.core.next.call(null,seq__36737_36751__$1);
var G__36759 = null;
var G__36760 = (0);
var G__36761 = (0);
seq__36737_36741 = G__36758;
chunk__36738_36742 = G__36759;
count__36739_36743 = G__36760;
i__36740_36744 = G__36761;
continue;
}
} else {
}
}
break;
}

var G__36762 = seq__36729;
var G__36763 = chunk__36734;
var G__36764 = count__36735;
var G__36765 = (i__36736 + (1));
seq__36729 = G__36762;
chunk__36734 = G__36763;
count__36735 = G__36764;
i__36736 = G__36765;
continue;
} else {
var temp__4425__auto__ = cljs.core.seq.call(null,seq__36729);
if(temp__4425__auto__){
var seq__36729__$1 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__36729__$1)){
var c__16854__auto__ = cljs.core.chunk_first.call(null,seq__36729__$1);
var G__36766 = cljs.core.chunk_rest.call(null,seq__36729__$1);
var G__36767 = c__16854__auto__;
var G__36768 = cljs.core.count.call(null,c__16854__auto__);
var G__36769 = (0);
seq__36729 = G__36766;
chunk__36734 = G__36767;
count__36735 = G__36768;
i__36736 = G__36769;
continue;
} else {
var h_name = cljs.core.first.call(null,seq__36729__$1);
var seq__36730_36770 = cljs.core.seq.call(null,cljs.core.vals.call(null,headers));
var chunk__36731_36771 = null;
var count__36732_36772 = (0);
var i__36733_36773 = (0);
while(true){
if((i__36733_36773 < count__36732_36772)){
var h_val_36774 = cljs.core._nth.call(null,chunk__36731_36771,i__36733_36773);
xhr.headers.set(h_name,h_val_36774);

var G__36775 = seq__36730_36770;
var G__36776 = chunk__36731_36771;
var G__36777 = count__36732_36772;
var G__36778 = (i__36733_36773 + (1));
seq__36730_36770 = G__36775;
chunk__36731_36771 = G__36776;
count__36732_36772 = G__36777;
i__36733_36773 = G__36778;
continue;
} else {
var temp__4425__auto___36779__$1 = cljs.core.seq.call(null,seq__36730_36770);
if(temp__4425__auto___36779__$1){
var seq__36730_36780__$1 = temp__4425__auto___36779__$1;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__36730_36780__$1)){
var c__16854__auto___36781 = cljs.core.chunk_first.call(null,seq__36730_36780__$1);
var G__36782 = cljs.core.chunk_rest.call(null,seq__36730_36780__$1);
var G__36783 = c__16854__auto___36781;
var G__36784 = cljs.core.count.call(null,c__16854__auto___36781);
var G__36785 = (0);
seq__36730_36770 = G__36782;
chunk__36731_36771 = G__36783;
count__36732_36772 = G__36784;
i__36733_36773 = G__36785;
continue;
} else {
var h_val_36786 = cljs.core.first.call(null,seq__36730_36780__$1);
xhr.headers.set(h_name,h_val_36786);

var G__36787 = cljs.core.next.call(null,seq__36730_36780__$1);
var G__36788 = null;
var G__36789 = (0);
var G__36790 = (0);
seq__36730_36770 = G__36787;
chunk__36731_36771 = G__36788;
count__36732_36772 = G__36789;
i__36733_36773 = G__36790;
continue;
}
} else {
}
}
break;
}

var G__36791 = cljs.core.next.call(null,seq__36729__$1);
var G__36792 = null;
var G__36793 = (0);
var G__36794 = (0);
seq__36729 = G__36791;
chunk__36734 = G__36792;
count__36735 = G__36793;
i__36736 = G__36794;
continue;
}
} else {
return null;
}
}
break;
}
});
/**
 * Takes an XhrIo object and sets response-type if not nil.
 */
cljs_http.core.apply_response_type_BANG_ = (function cljs_http$core$apply_response_type_BANG_(xhr,response_type){
return xhr.setResponseType((function (){var G__36796 = response_type;
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"array-buffer","array-buffer",519008380),G__36796)){
return goog.net.XhrIo.ResponseType.ARRAY_BUFFER;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"blob","blob",1636965233),G__36796)){
return goog.net.XhrIo.ResponseType.BLOB;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"document","document",-1329188687),G__36796)){
return goog.net.XhrIo.ResponseType.DOCUMENT;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"text","text",-1790561697),G__36796)){
return goog.net.XhrIo.ResponseType.TEXT;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"default","default",-1987822328),G__36796)){
return goog.net.XhrIo.ResponseType.DEFAULT;
} else {
if(cljs.core._EQ_.call(null,null,G__36796)){
return goog.net.XhrIo.ResponseType.DEFAULT;
} else {
throw (new Error([cljs.core.str("No matching clause: "),cljs.core.str(response_type)].join('')));

}
}
}
}
}
}
})());
});
/**
 * Builds an XhrIo object from the request parameters.
 */
cljs_http.core.build_xhr = (function cljs_http$core$build_xhr(p__36797){
var map__36800 = p__36797;
var map__36800__$1 = ((cljs.core.seq_QMARK_.call(null,map__36800))?cljs.core.apply.call(null,cljs.core.hash_map,map__36800):map__36800);
var request = map__36800__$1;
var with_credentials_QMARK_ = cljs.core.get.call(null,map__36800__$1,new cljs.core.Keyword(null,"with-credentials?","with-credentials?",-1773202222));
var default_headers = cljs.core.get.call(null,map__36800__$1,new cljs.core.Keyword(null,"default-headers","default-headers",-43146094));
var response_type = cljs.core.get.call(null,map__36800__$1,new cljs.core.Keyword(null,"response-type","response-type",-1493770458));
var timeout = (function (){var or__16069__auto__ = new cljs.core.Keyword(null,"timeout","timeout",-318625318).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return (0);
}
})();
var send_credentials = (((with_credentials_QMARK_ == null))?true:with_credentials_QMARK_);
var G__36801 = (new goog.net.XhrIo());
cljs_http.core.apply_default_headers_BANG_.call(null,G__36801,default_headers);

cljs_http.core.apply_response_type_BANG_.call(null,G__36801,response_type);

G__36801.setTimeoutInterval(timeout);

G__36801.setWithCredentials(send_credentials);

return G__36801;
});
cljs_http.core.error_kw = cljs.core.PersistentHashMap.fromArrays([(0),(7),(1),(4),(6),(3),(2),(9),(5),(8)],[new cljs.core.Keyword(null,"no-error","no-error",1984610064),new cljs.core.Keyword(null,"abort","abort",521193198),new cljs.core.Keyword(null,"access-denied","access-denied",959449406),new cljs.core.Keyword(null,"custom-error","custom-error",-1565161123),new cljs.core.Keyword(null,"http-error","http-error",-1040049553),new cljs.core.Keyword(null,"ff-silent-error","ff-silent-error",189390514),new cljs.core.Keyword(null,"file-not-found","file-not-found",-65398940),new cljs.core.Keyword(null,"offline","offline",-107631935),new cljs.core.Keyword(null,"exception","exception",-335277064),new cljs.core.Keyword(null,"timeout","timeout",-318625318)]);
/**
 * Execute the HTTP request corresponding to the given Ring request
 * map and return a core.async channel.
 */
cljs_http.core.xhr = (function cljs_http$core$xhr(p__36802){
var map__36828 = p__36802;
var map__36828__$1 = ((cljs.core.seq_QMARK_.call(null,map__36828))?cljs.core.apply.call(null,cljs.core.hash_map,map__36828):map__36828);
var request = map__36828__$1;
var request_method = cljs.core.get.call(null,map__36828__$1,new cljs.core.Keyword(null,"request-method","request-method",1764796830));
var headers = cljs.core.get.call(null,map__36828__$1,new cljs.core.Keyword(null,"headers","headers",-835030129));
var body = cljs.core.get.call(null,map__36828__$1,new cljs.core.Keyword(null,"body","body",-2049205669));
var with_credentials_QMARK_ = cljs.core.get.call(null,map__36828__$1,new cljs.core.Keyword(null,"with-credentials?","with-credentials?",-1773202222));
var cancel = cljs.core.get.call(null,map__36828__$1,new cljs.core.Keyword(null,"cancel","cancel",-1964088360));
var channel = cljs.core.async.chan.call(null);
var request_url = cljs_http.util.build_url.call(null,request);
var method = cljs.core.name.call(null,(function (){var or__16069__auto__ = request_method;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return new cljs.core.Keyword(null,"get","get",1683182755);
}
})());
var headers__$1 = cljs_http.util.build_headers.call(null,headers);
var xhr__$1 = cljs_http.core.build_xhr.call(null,request);
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.assoc,channel,xhr__$1);

xhr__$1.listen(goog.net.EventType.COMPLETE,((function (channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function (evt){
var target = evt.target;
var response = new cljs.core.PersistentArrayMap(null, 7, [new cljs.core.Keyword(null,"status","status",-1997798413),target.getStatus(),new cljs.core.Keyword(null,"success","success",1890645906),target.isSuccess(),new cljs.core.Keyword(null,"body","body",-2049205669),target.getResponse(),new cljs.core.Keyword(null,"headers","headers",-835030129),cljs_http.util.parse_headers.call(null,target.getAllResponseHeaders()),new cljs.core.Keyword(null,"trace-redirects","trace-redirects",-1149427907),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [request_url,target.getLastUri()], null),new cljs.core.Keyword(null,"error-code","error-code",180497232),cljs_http.core.error_kw.call(null,target.getLastErrorCode()),new cljs.core.Keyword(null,"error-text","error-text",2021893718),target.getLastError()], null);
if(cljs.core.not.call(null,cljs_http.core.aborted_QMARK_.call(null,xhr__$1))){
cljs.core.async.put_BANG_.call(null,channel,response);
} else {
}

cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

if(cljs.core.truth_(cancel)){
cljs.core.async.close_BANG_.call(null,cancel);
} else {
}

return cljs.core.async.close_BANG_.call(null,channel);
});})(channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
);

xhr__$1.send(request_url,method,body,headers__$1);

if(cljs.core.truth_(cancel)){
var c__23633__auto___36853 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___36853,channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___36853,channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function (state_36839){
var state_val_36840 = (state_36839[(1)]);
if((state_val_36840 === (1))){
var state_36839__$1 = state_36839;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_36839__$1,(2),cancel);
} else {
if((state_val_36840 === (2))){
var inst_36830 = (state_36839[(2)]);
var inst_36831 = xhr__$1.isComplete();
var inst_36832 = cljs.core.not.call(null,inst_36831);
var state_36839__$1 = (function (){var statearr_36841 = state_36839;
(statearr_36841[(7)] = inst_36830);

return statearr_36841;
})();
if(inst_36832){
var statearr_36842_36854 = state_36839__$1;
(statearr_36842_36854[(1)] = (3));

} else {
var statearr_36843_36855 = state_36839__$1;
(statearr_36843_36855[(1)] = (4));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_36840 === (3))){
var inst_36834 = xhr__$1.abort();
var state_36839__$1 = state_36839;
var statearr_36844_36856 = state_36839__$1;
(statearr_36844_36856[(2)] = inst_36834);

(statearr_36844_36856[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_36840 === (4))){
var state_36839__$1 = state_36839;
var statearr_36845_36857 = state_36839__$1;
(statearr_36845_36857[(2)] = null);

(statearr_36845_36857[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_36840 === (5))){
var inst_36837 = (state_36839[(2)]);
var state_36839__$1 = state_36839;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_36839__$1,inst_36837);
} else {
return null;
}
}
}
}
}
});})(c__23633__auto___36853,channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
;
return ((function (switch__23571__auto__,c__23633__auto___36853,channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function() {
var cljs_http$core$xhr_$_state_machine__23572__auto__ = null;
var cljs_http$core$xhr_$_state_machine__23572__auto____0 = (function (){
var statearr_36849 = [null,null,null,null,null,null,null,null];
(statearr_36849[(0)] = cljs_http$core$xhr_$_state_machine__23572__auto__);

(statearr_36849[(1)] = (1));

return statearr_36849;
});
var cljs_http$core$xhr_$_state_machine__23572__auto____1 = (function (state_36839){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_36839);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e36850){if((e36850 instanceof Object)){
var ex__23575__auto__ = e36850;
var statearr_36851_36858 = state_36839;
(statearr_36851_36858[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_36839);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e36850;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__36859 = state_36839;
state_36839 = G__36859;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs_http$core$xhr_$_state_machine__23572__auto__ = function(state_36839){
switch(arguments.length){
case 0:
return cljs_http$core$xhr_$_state_machine__23572__auto____0.call(this);
case 1:
return cljs_http$core$xhr_$_state_machine__23572__auto____1.call(this,state_36839);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs_http$core$xhr_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs_http$core$xhr_$_state_machine__23572__auto____0;
cljs_http$core$xhr_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs_http$core$xhr_$_state_machine__23572__auto____1;
return cljs_http$core$xhr_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___36853,channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
})();
var state__23635__auto__ = (function (){var statearr_36852 = f__23634__auto__.call(null);
(statearr_36852[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___36853);

return statearr_36852;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___36853,channel,request_url,method,headers__$1,xhr__$1,map__36828,map__36828__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
);

} else {
}

return channel;
});
/**
 * Execute the JSONP request corresponding to the given Ring request
 * map and return a core.async channel.
 */
cljs_http.core.jsonp = (function cljs_http$core$jsonp(p__36860){
var map__36876 = p__36860;
var map__36876__$1 = ((cljs.core.seq_QMARK_.call(null,map__36876))?cljs.core.apply.call(null,cljs.core.hash_map,map__36876):map__36876);
var request = map__36876__$1;
var timeout = cljs.core.get.call(null,map__36876__$1,new cljs.core.Keyword(null,"timeout","timeout",-318625318));
var callback_name = cljs.core.get.call(null,map__36876__$1,new cljs.core.Keyword(null,"callback-name","callback-name",336964714));
var cancel = cljs.core.get.call(null,map__36876__$1,new cljs.core.Keyword(null,"cancel","cancel",-1964088360));
var channel = cljs.core.async.chan.call(null);
var jsonp__$1 = (new goog.net.Jsonp(cljs_http.util.build_url.call(null,request),callback_name));
jsonp__$1.setRequestTimeout(timeout);

var req_36891 = jsonp__$1.send(null,((function (channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel){
return (function cljs_http$core$jsonp_$_success_callback(data){
var response = new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"status","status",-1997798413),(200),new cljs.core.Keyword(null,"success","success",1890645906),true,new cljs.core.Keyword(null,"body","body",-2049205669),cljs.core.js__GT_clj.call(null,data,new cljs.core.Keyword(null,"keywordize-keys","keywordize-keys",1310784252),true)], null);
cljs.core.async.put_BANG_.call(null,channel,response);

cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

if(cljs.core.truth_(cancel)){
cljs.core.async.close_BANG_.call(null,cancel);
} else {
}

return cljs.core.async.close_BANG_.call(null,channel);
});})(channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel))
,((function (channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel){
return (function cljs_http$core$jsonp_$_error_callback(){
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

if(cljs.core.truth_(cancel)){
cljs.core.async.close_BANG_.call(null,cancel);
} else {
}

return cljs.core.async.close_BANG_.call(null,channel);
});})(channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel))
);
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.assoc,channel,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"jsonp","jsonp",226119588),jsonp__$1,new cljs.core.Keyword(null,"request","request",1772954723),req_36891], null));

if(cljs.core.truth_(cancel)){
var c__23633__auto___36892 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___36892,req_36891,channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___36892,req_36891,channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel){
return (function (state_36881){
var state_val_36882 = (state_36881[(1)]);
if((state_val_36882 === (1))){
var state_36881__$1 = state_36881;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_36881__$1,(2),cancel);
} else {
if((state_val_36882 === (2))){
var inst_36878 = (state_36881[(2)]);
var inst_36879 = jsonp__$1.cancel(req_36891);
var state_36881__$1 = (function (){var statearr_36883 = state_36881;
(statearr_36883[(7)] = inst_36878);

return statearr_36883;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_36881__$1,inst_36879);
} else {
return null;
}
}
});})(c__23633__auto___36892,req_36891,channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel))
;
return ((function (switch__23571__auto__,c__23633__auto___36892,req_36891,channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel){
return (function() {
var cljs_http$core$jsonp_$_state_machine__23572__auto__ = null;
var cljs_http$core$jsonp_$_state_machine__23572__auto____0 = (function (){
var statearr_36887 = [null,null,null,null,null,null,null,null];
(statearr_36887[(0)] = cljs_http$core$jsonp_$_state_machine__23572__auto__);

(statearr_36887[(1)] = (1));

return statearr_36887;
});
var cljs_http$core$jsonp_$_state_machine__23572__auto____1 = (function (state_36881){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_36881);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e36888){if((e36888 instanceof Object)){
var ex__23575__auto__ = e36888;
var statearr_36889_36893 = state_36881;
(statearr_36889_36893[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_36881);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e36888;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__36894 = state_36881;
state_36881 = G__36894;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs_http$core$jsonp_$_state_machine__23572__auto__ = function(state_36881){
switch(arguments.length){
case 0:
return cljs_http$core$jsonp_$_state_machine__23572__auto____0.call(this);
case 1:
return cljs_http$core$jsonp_$_state_machine__23572__auto____1.call(this,state_36881);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs_http$core$jsonp_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs_http$core$jsonp_$_state_machine__23572__auto____0;
cljs_http$core$jsonp_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs_http$core$jsonp_$_state_machine__23572__auto____1;
return cljs_http$core$jsonp_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___36892,req_36891,channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel))
})();
var state__23635__auto__ = (function (){var statearr_36890 = f__23634__auto__.call(null);
(statearr_36890[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___36892);

return statearr_36890;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___36892,req_36891,channel,jsonp__$1,map__36876,map__36876__$1,request,timeout,callback_name,cancel))
);

} else {
}

return channel;
});
/**
 * Execute the HTTP request corresponding to the given Ring request
 * map and return a core.async channel.
 */
cljs_http.core.request = (function cljs_http$core$request(p__36895){
var map__36897 = p__36895;
var map__36897__$1 = ((cljs.core.seq_QMARK_.call(null,map__36897))?cljs.core.apply.call(null,cljs.core.hash_map,map__36897):map__36897);
var request__$1 = map__36897__$1;
var request_method = cljs.core.get.call(null,map__36897__$1,new cljs.core.Keyword(null,"request-method","request-method",1764796830));
if(cljs.core._EQ_.call(null,request_method,new cljs.core.Keyword(null,"jsonp","jsonp",226119588))){
return cljs_http.core.jsonp.call(null,request__$1);
} else {
return cljs_http.core.xhr.call(null,request__$1);
}
});

//# sourceMappingURL=core.js.map?rel=1439206052635
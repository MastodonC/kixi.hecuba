// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.ndarray.contours');
goog.require('cljs.core');
goog.require('thi.ng.ndarray.core');
goog.require('thi.ng.typedarrays.core');
thi.ng.ndarray.contours.level_crossing = (function thi$ng$ndarray$contours$level_crossing(offset,a,b,level){
var da = (a - level);
var db = (b - level);
if(!(cljs.core._EQ_.call(null,(da >= 0.0),(db >= 0.0)))){
return (offset + (0.5 + (0.5 * ((da + db) / (da - db)))));
} else {
return null;
}
});
thi.ng.ndarray.contours.level_crossings1d = (function thi$ng$ndarray$contours$level_crossings1d(mat,shape,level){
var iter__16823__auto__ = (function thi$ng$ndarray$contours$level_crossings1d_$_iter__32907(s__32908){
return (new cljs.core.LazySeq(null,(function (){
var s__32908__$1 = s__32908;
while(true){
var temp__4425__auto__ = cljs.core.seq.call(null,s__32908__$1);
if(temp__4425__auto__){
var s__32908__$2 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,s__32908__$2)){
var c__16821__auto__ = cljs.core.chunk_first.call(null,s__32908__$2);
var size__16822__auto__ = cljs.core.count.call(null,c__16821__auto__);
var b__32910 = cljs.core.chunk_buffer.call(null,size__16822__auto__);
if((function (){var i__32909 = (0);
while(true){
if((i__32909 < size__16822__auto__)){
var x = cljs.core._nth.call(null,c__16821__auto__,i__32909);
var x_SINGLEQUOTE_ = thi.ng.ndarray.contours.level_crossing.call(null,x,thi.ng.ndarray.core.get_at.call(null,mat,x),thi.ng.ndarray.core.get_at.call(null,mat,(x + (1))),level);
if(cljs.core.truth_(x_SINGLEQUOTE_)){
cljs.core.chunk_append.call(null,b__32910,x_SINGLEQUOTE_);

var G__32911 = (i__32909 + (1));
i__32909 = G__32911;
continue;
} else {
var G__32912 = (i__32909 + (1));
i__32909 = G__32912;
continue;
}
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32910),thi$ng$ndarray$contours$level_crossings1d_$_iter__32907.call(null,cljs.core.chunk_rest.call(null,s__32908__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__32910),null);
}
} else {
var x = cljs.core.first.call(null,s__32908__$2);
var x_SINGLEQUOTE_ = thi.ng.ndarray.contours.level_crossing.call(null,x,thi.ng.ndarray.core.get_at.call(null,mat,x),thi.ng.ndarray.core.get_at.call(null,mat,(x + (1))),level);
if(cljs.core.truth_(x_SINGLEQUOTE_)){
return cljs.core.cons.call(null,x_SINGLEQUOTE_,thi$ng$ndarray$contours$level_crossings1d_$_iter__32907.call(null,cljs.core.rest.call(null,s__32908__$2)));
} else {
var G__32913 = cljs.core.rest.call(null,s__32908__$2);
s__32908__$1 = G__32913;
continue;
}
}
} else {
return null;
}
break;
}
}),null,null));
});
return iter__16823__auto__.call(null,cljs.core.range.call(null,(((typeof shape === 'number')?shape:cljs.core.first.call(null,shape)) - (1))));
});
thi.ng.ndarray.contours.level_crossings2d_x = (function thi$ng$ndarray$contours$level_crossings2d_x(){
var G__32916 = arguments.length;
switch (G__32916) {
case 2:
return thi.ng.ndarray.contours.level_crossings2d_x.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.ndarray.contours.level_crossings2d_x.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.ndarray.contours.level_crossings2d_x.cljs$core$IFn$_invoke$arity$2 = (function (mat,level){
return thi.ng.ndarray.contours.level_crossings2d_x.call(null,mat,thi.ng.ndarray.core.shape.call(null,mat),level);
});

thi.ng.ndarray.contours.level_crossings2d_x.cljs$core$IFn$_invoke$arity$3 = (function (mat,p__32917,level){
var vec__32918 = p__32917;
var sy = cljs.core.nth.call(null,vec__32918,(0),null);
var sx = cljs.core.nth.call(null,vec__32918,(1),null);
return cljs.core.mapcat.call(null,((function (vec__32918,sy,sx){
return (function (y){
return cljs.core.map.call(null,((function (vec__32918,sy,sx){
return (function (p1__32914_SHARP_){
return (new cljs.core.PersistentVector(null,2,(5),cljs.core.PersistentVector.EMPTY_NODE,[y,p1__32914_SHARP_],null));
});})(vec__32918,sy,sx))
,thi.ng.ndarray.contours.level_crossings1d.call(null,thi.ng.ndarray.core.pick.call(null,mat,y,null),sx,level));
});})(vec__32918,sy,sx))
,cljs.core.range.call(null,sy));
});

thi.ng.ndarray.contours.level_crossings2d_x.cljs$lang$maxFixedArity = 3;
thi.ng.ndarray.contours.level_crossings2d_y = (function thi$ng$ndarray$contours$level_crossings2d_y(){
var G__32922 = arguments.length;
switch (G__32922) {
case 2:
return thi.ng.ndarray.contours.level_crossings2d_y.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.ndarray.contours.level_crossings2d_y.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.ndarray.contours.level_crossings2d_y.cljs$core$IFn$_invoke$arity$2 = (function (mat,level){
return thi.ng.ndarray.contours.level_crossings2d_y.call(null,mat,thi.ng.ndarray.core.shape.call(null,mat),level);
});

thi.ng.ndarray.contours.level_crossings2d_y.cljs$core$IFn$_invoke$arity$3 = (function (mat,p__32923,level){
var vec__32924 = p__32923;
var sy = cljs.core.nth.call(null,vec__32924,(0),null);
var sx = cljs.core.nth.call(null,vec__32924,(1),null);
return cljs.core.mapcat.call(null,((function (vec__32924,sy,sx){
return (function (x){
return cljs.core.map.call(null,((function (vec__32924,sy,sx){
return (function (p1__32920_SHARP_){
return (new cljs.core.PersistentVector(null,2,(5),cljs.core.PersistentVector.EMPTY_NODE,[p1__32920_SHARP_,x],null));
});})(vec__32924,sy,sx))
,thi.ng.ndarray.contours.level_crossings1d.call(null,thi.ng.ndarray.core.pick.call(null,mat,null,x),sy,level));
});})(vec__32924,sy,sx))
,cljs.core.range.call(null,sx));
});

thi.ng.ndarray.contours.level_crossings2d_y.cljs$lang$maxFixedArity = 3;
thi.ng.ndarray.contours.level_crossings2d = (function thi$ng$ndarray$contours$level_crossings2d(){
var G__32927 = arguments.length;
switch (G__32927) {
case 2:
return thi.ng.ndarray.contours.level_crossings2d.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.ndarray.contours.level_crossings2d.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.ndarray.contours.level_crossings2d.cljs$core$IFn$_invoke$arity$2 = (function (mat,level){
return thi.ng.ndarray.contours.level_crossings2d.call(null,mat,thi.ng.ndarray.core.shape.call(null,mat),level);
});

thi.ng.ndarray.contours.level_crossings2d.cljs$core$IFn$_invoke$arity$3 = (function (mat,shape,level){
return cljs.core.concat.call(null,thi.ng.ndarray.contours.level_crossings2d_x.call(null,mat,shape,level),thi.ng.ndarray.contours.level_crossings2d_y.call(null,mat,shape,level));
});

thi.ng.ndarray.contours.level_crossings2d.cljs$lang$maxFixedArity = 3;
thi.ng.ndarray.contours.level_crossings3d_x = (function thi$ng$ndarray$contours$level_crossings3d_x(){
var G__32931 = arguments.length;
switch (G__32931) {
case 2:
return thi.ng.ndarray.contours.level_crossings3d_x.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.ndarray.contours.level_crossings3d_x.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.ndarray.contours.level_crossings3d_x.cljs$core$IFn$_invoke$arity$2 = (function (mat,level){
return thi.ng.ndarray.contours.level_crossings3d_x.call(null,mat,thi.ng.ndarray.core.shape.call(null,mat),level);
});

thi.ng.ndarray.contours.level_crossings3d_x.cljs$core$IFn$_invoke$arity$3 = (function (mat,p__32932,level){
var vec__32933 = p__32932;
var sz = cljs.core.nth.call(null,vec__32933,(0),null);
var sy = cljs.core.nth.call(null,vec__32933,(1),null);
var sx = cljs.core.nth.call(null,vec__32933,(2),null);
return cljs.core.mapcat.call(null,((function (vec__32933,sz,sy,sx){
return (function (z){
return cljs.core.map.call(null,((function (vec__32933,sz,sy,sx){
return (function (p1__32929_SHARP_){
return cljs.core.cons.call(null,z,p1__32929_SHARP_);
});})(vec__32933,sz,sy,sx))
,thi.ng.ndarray.contours.level_crossings2d_x.call(null,thi.ng.ndarray.core.pick.call(null,mat,z,null,null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [sy,sx], null),level));
});})(vec__32933,sz,sy,sx))
,cljs.core.range.call(null,sz));
});

thi.ng.ndarray.contours.level_crossings3d_x.cljs$lang$maxFixedArity = 3;
thi.ng.ndarray.contours.level_crossings3d_y = (function thi$ng$ndarray$contours$level_crossings3d_y(){
var G__32937 = arguments.length;
switch (G__32937) {
case 2:
return thi.ng.ndarray.contours.level_crossings3d_y.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.ndarray.contours.level_crossings3d_y.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.ndarray.contours.level_crossings3d_y.cljs$core$IFn$_invoke$arity$2 = (function (mat,level){
return thi.ng.ndarray.contours.level_crossings3d_y.call(null,mat,thi.ng.ndarray.core.shape.call(null,mat),level);
});

thi.ng.ndarray.contours.level_crossings3d_y.cljs$core$IFn$_invoke$arity$3 = (function (mat,p__32938,level){
var vec__32939 = p__32938;
var sz = cljs.core.nth.call(null,vec__32939,(0),null);
var sy = cljs.core.nth.call(null,vec__32939,(1),null);
var sx = cljs.core.nth.call(null,vec__32939,(2),null);
return cljs.core.mapcat.call(null,((function (vec__32939,sz,sy,sx){
return (function (z){
return cljs.core.map.call(null,((function (vec__32939,sz,sy,sx){
return (function (p1__32935_SHARP_){
return cljs.core.cons.call(null,z,p1__32935_SHARP_);
});})(vec__32939,sz,sy,sx))
,thi.ng.ndarray.contours.level_crossings2d_y.call(null,thi.ng.ndarray.core.pick.call(null,mat,z,null,null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [sy,sx], null),level));
});})(vec__32939,sz,sy,sx))
,cljs.core.range.call(null,sz));
});

thi.ng.ndarray.contours.level_crossings3d_y.cljs$lang$maxFixedArity = 3;
thi.ng.ndarray.contours.level_crossings3d_z = (function thi$ng$ndarray$contours$level_crossings3d_z(){
var G__32943 = arguments.length;
switch (G__32943) {
case 2:
return thi.ng.ndarray.contours.level_crossings3d_z.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.ndarray.contours.level_crossings3d_z.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.ndarray.contours.level_crossings3d_z.cljs$core$IFn$_invoke$arity$2 = (function (mat,level){
return thi.ng.ndarray.contours.level_crossings3d_z.call(null,mat,thi.ng.ndarray.core.shape.call(null,mat),level);
});

thi.ng.ndarray.contours.level_crossings3d_z.cljs$core$IFn$_invoke$arity$3 = (function (mat,p__32944,level){
var vec__32945 = p__32944;
var sz = cljs.core.nth.call(null,vec__32945,(0),null);
var sy = cljs.core.nth.call(null,vec__32945,(1),null);
var sx = cljs.core.nth.call(null,vec__32945,(2),null);
return cljs.core.mapcat.call(null,((function (vec__32945,sz,sy,sx){
return (function (x){
return cljs.core.map.call(null,((function (vec__32945,sz,sy,sx){
return (function (p1__32941_SHARP_){
return cljs.core.conj.call(null,p1__32941_SHARP_,x);
});})(vec__32945,sz,sy,sx))
,thi.ng.ndarray.contours.level_crossings2d_y.call(null,thi.ng.ndarray.core.pick.call(null,mat,null,null,x),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [sz,sy], null),level));
});})(vec__32945,sz,sy,sx))
,cljs.core.range.call(null,sx));
});

thi.ng.ndarray.contours.level_crossings3d_z.cljs$lang$maxFixedArity = 3;
thi.ng.ndarray.contours.level_crossings3d = (function thi$ng$ndarray$contours$level_crossings3d(){
var G__32948 = arguments.length;
switch (G__32948) {
case 2:
return thi.ng.ndarray.contours.level_crossings3d.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.ndarray.contours.level_crossings3d.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.ndarray.contours.level_crossings3d.cljs$core$IFn$_invoke$arity$2 = (function (mat,level){
return thi.ng.ndarray.contours.level_crossings3d.call(null,mat,thi.ng.ndarray.core.shape.call(null,mat),level);
});

thi.ng.ndarray.contours.level_crossings3d.cljs$core$IFn$_invoke$arity$3 = (function (mat,shape,level){
return cljs.core.concat.call(null,thi.ng.ndarray.contours.level_crossings3d_x.call(null,mat,shape,level),thi.ng.ndarray.contours.level_crossings3d_y.call(null,mat,shape,level),thi.ng.ndarray.contours.level_crossings3d_z.call(null,mat,shape,level));
});

thi.ng.ndarray.contours.level_crossings3d.cljs$lang$maxFixedArity = 3;
thi.ng.ndarray.contours.edge_index_2d = new cljs.core.PersistentVector(null, 16, 5, cljs.core.PersistentVector.EMPTY_NODE, [null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(2),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(0)], null),null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(3),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(2),(0)], null),null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(3),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(2),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(3),(0)], null),null], null);
thi.ng.ndarray.contours.next_edges_2d = new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(-1),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(1)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1),(0)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(-1)], null)], null);
thi.ng.ndarray.contours.set_border_2d = (function thi$ng$ndarray$contours$set_border_2d(mat,x){
var vec__32951 = thi.ng.ndarray.core.shape.call(null,mat);
var h = cljs.core.nth.call(null,vec__32951,(0),null);
var w = cljs.core.nth.call(null,vec__32951,(1),null);
var h_SINGLEQUOTE_ = (h - (1));
var w_SINGLEQUOTE_ = (w - (1));
var l = thi.ng.ndarray.core.pick.call(null,mat,null,(0));
var r = thi.ng.ndarray.core.pick.call(null,mat,null,w_SINGLEQUOTE_);
var t = thi.ng.ndarray.core.pick.call(null,mat,(0),null);
var b = thi.ng.ndarray.core.pick.call(null,mat,h_SINGLEQUOTE_,null);
var i_32952 = w_SINGLEQUOTE_;
while(true){
if((i_32952 >= (0))){
thi.ng.ndarray.core.set_at.call(null,t,i_32952,x);

thi.ng.ndarray.core.set_at.call(null,b,i_32952,x);

var G__32953 = (i_32952 - (1));
i_32952 = G__32953;
continue;
} else {
}
break;
}

var i_32954 = h_SINGLEQUOTE_;
while(true){
if((i_32954 >= (0))){
thi.ng.ndarray.core.set_at.call(null,l,i_32954,x);

thi.ng.ndarray.core.set_at.call(null,r,i_32954,x);

var G__32955 = (i_32954 - (1));
i_32954 = G__32955;
continue;
} else {
}
break;
}

return mat;
});
thi.ng.ndarray.contours.encode_crossings_2d = (function thi$ng$ndarray$contours$encode_crossings_2d(src,isoval){
var out = thi.ng.ndarray.core.ndarray.call(null,new cljs.core.Keyword(null,"int8","int8",-1834023920),thi.ng.typedarrays.core.int8.call(null,thi.ng.ndarray.core.size.call(null,src)),thi.ng.ndarray.core.shape.call(null,src));
var iso_QMARK_ = ((function (out){
return (function (y,x,m){
if((thi.ng.ndarray.core.get_at.call(null,src,y,x) < isoval)){
return m;
} else {
return (0);
}
});})(out))
;
var pos = thi.ng.ndarray.core.position_seq.call(null,thi.ng.ndarray.core.truncate_h.call(null,src,(-1),(-1)));
while(true){
if(cljs.core.truth_(pos)){
var vec__32957 = cljs.core.first.call(null,pos);
var y = cljs.core.nth.call(null,vec__32957,(0),null);
var x = cljs.core.nth.call(null,vec__32957,(1),null);
var x_SINGLEQUOTE_ = (x + (1));
var y_SINGLEQUOTE_ = (y + (1));
thi.ng.ndarray.core.set_at.call(null,out,y,x,(((iso_QMARK_.call(null,y,x,(8)) | iso_QMARK_.call(null,y,x_SINGLEQUOTE_,(4))) | iso_QMARK_.call(null,y_SINGLEQUOTE_,x_SINGLEQUOTE_,(2))) | iso_QMARK_.call(null,y_SINGLEQUOTE_,x,(1))));

var G__32958 = cljs.core.next.call(null,pos);
pos = G__32958;
continue;
} else {
return out;
}
break;
}
});
thi.ng.ndarray.contours.mean_cell_value_2d = (function thi$ng$ndarray$contours$mean_cell_value_2d(src,y,x){
return (((thi.ng.ndarray.core.get_at.call(null,src,y,x) + thi.ng.ndarray.core.get_at.call(null,src,y,(x + (1)))) + (thi.ng.ndarray.core.get_at.call(null,src,(y + (1)),x) + thi.ng.ndarray.core.get_at.call(null,src,(y + (1)),(x + (1))))) * 0.25);
});
thi.ng.ndarray.contours.process_saddle5 = (function thi$ng$ndarray$contours$process_saddle5(src,y,x,iso,from){
if((thi.ng.ndarray.contours.mean_cell_value_2d.call(null,src,y,x) > iso)){
if(((3) === from)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(2),(4)], null);
} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(1)], null);
}
} else {
if(((3) === from)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(0),(13)], null);
} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(2),(7)], null);
}
}
});
thi.ng.ndarray.contours.process_saddle10 = (function thi$ng$ndarray$contours$process_saddle10(src,y,x,iso,from){
if((thi.ng.ndarray.contours.mean_cell_value_2d.call(null,src,y,x) > iso)){
if(((0) === from)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(3),(2)], null);
} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1),(8)], null);
}
} else {
if(((2) === from)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(3),(11)], null);
} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(1),(14)], null);
}
}
});
thi.ng.ndarray.contours.mix2d = (function thi$ng$ndarray$contours$mix2d(src,y1,x1,y2,x2,iso){
var a = thi.ng.ndarray.core.get_at.call(null,src,y1,x1);
var b = thi.ng.ndarray.core.get_at.call(null,src,y2,x2);
if((a === b)){
return (0);
} else {
return ((a - iso) / (a - b));
}
});
thi.ng.ndarray.contours.contour_vertex_2d = (function thi$ng$ndarray$contours$contour_vertex_2d(src,y,x,to,iso){
var x_SINGLEQUOTE_ = (x + (1));
var y_SINGLEQUOTE_ = (y + (1));
var G__32960 = (to | (0));
switch (G__32960) {
case (0):
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [y,(x + thi.ng.ndarray.contours.mix2d.call(null,src,y,x,y,x_SINGLEQUOTE_,iso))], null);

break;
case (1):
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(y + thi.ng.ndarray.contours.mix2d.call(null,src,y,x_SINGLEQUOTE_,y_SINGLEQUOTE_,x_SINGLEQUOTE_,iso)),x_SINGLEQUOTE_], null);

break;
case (2):
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [y_SINGLEQUOTE_,(x + thi.ng.ndarray.contours.mix2d.call(null,src,y_SINGLEQUOTE_,x,y_SINGLEQUOTE_,x_SINGLEQUOTE_,iso))], null);

break;
case (3):
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(y + thi.ng.ndarray.contours.mix2d.call(null,src,y,x,y_SINGLEQUOTE_,x,iso)),x], null);

break;
default:
return null;

}
});
thi.ng.ndarray.contours.find_contours_2d = (function thi$ng$ndarray$contours$find_contours_2d(src,isoval){
var vec__32967 = thi.ng.ndarray.core.shape.call(null,src);
var h_SINGLEQUOTE_ = cljs.core.nth.call(null,vec__32967,(0),null);
var w_SINGLEQUOTE_ = cljs.core.nth.call(null,vec__32967,(1),null);
var h_SINGLEQUOTE___$1 = (h_SINGLEQUOTE_ - (1));
var w_SINGLEQUOTE___$1 = (w_SINGLEQUOTE_ - (1));
var coded = thi.ng.ndarray.contours.encode_crossings_2d.call(null,src,isoval);
var contours = cljs.core.volatile_BANG_.call(null,cljs.core.transient$.call(null,cljs.core.PersistentVector.EMPTY));
var pos = thi.ng.ndarray.core.position_seq.call(null,coded);
var curr = cljs.core.transient$.call(null,cljs.core.PersistentVector.EMPTY);
var to = null;
var p = null;
while(true){
if(cljs.core.truth_(pos)){
var from = to;
var vec__32968 = (cljs.core.truth_(p)?p:cljs.core.first.call(null,pos));
var y = cljs.core.nth.call(null,vec__32968,(0),null);
var x = cljs.core.nth.call(null,vec__32968,(1),null);
if(((x >= w_SINGLEQUOTE___$1)) || ((y >= h_SINGLEQUOTE___$1))){
var G__32972 = cljs.core.next.call(null,pos);
var G__32973 = curr;
var G__32974 = to;
var G__32975 = null;
pos = G__32972;
curr = G__32973;
to = G__32974;
p = G__32975;
continue;
} else {
var id = thi.ng.ndarray.core.get_at.call(null,coded,y,x);
var vec__32969 = (function (){var G__32970 = (id | (0));
switch (G__32970) {
case (5):
return thi.ng.ndarray.contours.process_saddle5.call(null,src,y,x,isoval,from);

break;
case (10):
return thi.ng.ndarray.contours.process_saddle10.call(null,src,y,x,isoval,from);

break;
default:
return thi.ng.ndarray.contours.edge_index_2d.call(null,(id | (0)));

}
})();
var to__$1 = cljs.core.nth.call(null,vec__32969,(0),null);
var clear = cljs.core.nth.call(null,vec__32969,(1),null);
var curr__$1 = (cljs.core.truth_((function (){var and__16057__auto__ = (from == null);
if(and__16057__auto__){
var and__16057__auto____$1 = to__$1;
if(cljs.core.truth_(and__16057__auto____$1)){
return (cljs.core.count.call(null,curr) > (0));
} else {
return and__16057__auto____$1;
}
} else {
return and__16057__auto__;
}
})())?(function (){
cljs.core._vreset_BANG_.call(null,contours,cljs.core.conj_BANG_.call(null,cljs.core._deref.call(null,contours),cljs.core.persistent_BANG_.call(null,curr)));

return cljs.core.transient$.call(null,cljs.core.PersistentVector.EMPTY);
})()
:curr);
if(cljs.core.truth_(clear)){
thi.ng.ndarray.core.set_at.call(null,coded,y,x,clear);
} else {
}

if(cljs.core.truth_((function (){var and__16057__auto__ = to__$1;
if(cljs.core.truth_(and__16057__auto__)){
return (to__$1 >= (0));
} else {
return and__16057__auto__;
}
})())){
var vertex = thi.ng.ndarray.contours.contour_vertex_2d.call(null,src,y,x,to__$1,isoval);
var vec__32971 = thi.ng.ndarray.contours.next_edges_2d.call(null,to__$1);
var oy = cljs.core.nth.call(null,vec__32971,(0),null);
var ox = cljs.core.nth.call(null,vec__32971,(1),null);
var G__32977 = cljs.core.next.call(null,pos);
var G__32978 = cljs.core.conj_BANG_.call(null,curr__$1,vertex);
var G__32979 = to__$1;
var G__32980 = new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [(y + oy),(x + ox)], null);
pos = G__32977;
curr = G__32978;
to = G__32979;
p = G__32980;
continue;
} else {
var G__32981 = cljs.core.next.call(null,pos);
var G__32982 = curr__$1;
var G__32983 = to__$1;
var G__32984 = null;
pos = G__32981;
curr = G__32982;
to = G__32983;
p = G__32984;
continue;
}
}
} else {
return cljs.core.persistent_BANG_.call(null,cljs.core.conj_BANG_.call(null,cljs.core.deref.call(null,contours),cljs.core.persistent_BANG_.call(null,curr)));
}
break;
}
});

//# sourceMappingURL=contours.js.map?rel=1439206040535
POST /entities/_search?pretty
{
  "query" : {"query_string" : {"query" : "TSB"}}
}

POST /entities/_search?pretty
{
  "query" : {"query_string" : {"query" : "f17f0334-db71-4d55-8b54-e2270f147a99"}}
}


POST /entities/_search?pretty
{"query" :
 {
   "filtered" :
   {
     "query": {"query_string" : {"query" : "TSB"}},
     "filter": {
       "bool": {
         "must": { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"}},
         "should": [],
         "must_not": {}
       }
     }
   }
 }
}


POST /entities/_search?pretty
{"query" :
 {
   "filtered" :
   {
     "query": {"query_string" : {"query" : "photovoltaics:false"}},
     "filter": {
       "bool": {
         "must": { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"}},
         "should": [],
         "must_not": {}
       }
     }
   }
 }
}

POST localhost:9200/entities/_search?pretty
{"query" :
 {
   "filtered" :
   {
     "query": {"query_string" : {"query" : "photovoltaics:false"}},
     "filter": {
       "bool": {
         "must": {},
         "should": [
           { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"} },
           { "term": { "project_id" : "923be31f5e1d375dc626a861b0197ef27be75912"} }
         ],
         "must_not": {}
       }
     }
   }
 }
}

POST localhost:9200/entities/_search?pretty
{"query" :
 {
   "filtered" :
   {
     "query": {"query_string" : {"query" : "photovoltaics:false"}},
     "filter": {
       "bool": {
         "must": {},
         "should": [
           { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"} },
           { "term": { "project_id" : "923be31f5e1d375dc626a861b0197ef27be75912"} }
         ],
         "must_not": {}
       }
     }
   }
 }
}


POST localhost:9200/entities/_search?pretty
{"query" :
 {
   "filtered" :
   {
     "query": {"query_string" : {"query" : "photovoltaics:false"}},
     "filter": {
       "bool": {
         "must": {},
         "should": [
           { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"} },
           { "term": { "project_id" : "923be31f5e1d375dc626a861b0197ef27be75912"} }
         ],
         "must_not": {}
       }
     }
   }
 },
 "from": 0,
 "size": 20
}

POST localhost:9200/entities/_search?pretty
{
  "query" :
  {
    "filtered" :
    {
      "query": {"query_string" : {"query" : "TSB"}},
      "filter":
      {
        "bool":
        {
          "must":
          {
            "bool":
            {
              "must": {},
              "should":
              [
                { "term": { "programme_id" : "5a193dd5db5cec06b21d4868e35ac60aa6a4056d"} },
                { "term": { "programme_id" : "637fe13082db335ee40be0a144bfb5dd61bbe171"} }
              ],
              "must_not": {}
            }
          },
          "should":
          [
            { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"} },
            { "term": { "project_id" : "923be31f5e1d375dc626a861b0197ef27be75912"} }
          ],
          "must_not": {}
        }
      }
    }
  },
  "from": 0,
  "size": 10
}


POST localhost:9200/entities/_search?pretty
{"query" :
 {
   "filtered" :
   {
     "query": {"query_string" : {"query" : "photovoltaics:false"}},
     "filter": {
       "bool": {
         "must": { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"} },
         "should": [
           { "term": { "project_id" : "006118d7a68315d2a6bc40d552766cfd489798f7"} },
           { "term": { "public_access" : "true"} }
         ],
         "must_not": {}
       }
     }
   }
 }
}

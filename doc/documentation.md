### Advanced Search Functionality

The search functionality is based on elasticsearch and lucene and
provides powerful ways to create search terms.

The query string “mini-language” is used by the elasticsearch and
the search UI.

The query string is parsed into a series of terms and operators. A
term can be a single word -  quick or brown - or a phrase, surrounded
by double quotes - "quick brown" - which searches for all the words in
the phrase, in the same order.

Operators allow you to customize the search — the available options are explained below.

The following fields currently exist in as searchable fields:

 * address
 * property_code
 * entity_id
 * project_team
 * project_id
 * project_code
 * property_type
 * built_form
 * age
 * address_region
 * bedroom_count (a number)
 * heating_type
 * walls_construction
 * ventilation_systems (true/false)
 * photovoltaics (true/false)
 * solar_termals (true/false)
 * wind_turbines (true/false)
 * small_hydros (true/false)
 * heat_pumps (true/false)
 * chps (true/false)

Fields can be specified like this:

```
photovoltaics:true
```

To retrieve properties with photovoltaics.

Where the built_form is either a House or a Flat

```
built_form:(House OR Flat)
```

Where the address is exactly

```
address:"Buckingham Palace, London SW1A 1AA"
```

Where the field has no value or is missing:

```
_missing_:built_form
```


Where the field property_type has any non-null value:

```
_exists_:property_type
```

#### Wildcards

Wildcard searches can be run on individual terms, using ? to replace a
single character, and * to replace zero or more characters:

```
qu?ck bro*
```

Be aware that wildcard queries can use an enormous amount of memory
and perform very badly - just think how many terms need to be queried
to match the query string ```"a* b* c*"```.

Warning

Allowing a wildcard at the beginning of a word (eg "*ing") is
particularly heavy, because all terms in the index need to be
examined, just in case they match. Leading wildcards can be disabled
by setting allow_leading_wildcard to false.

Wildcarded terms are not analyzed by default - they are lowercased
(```lowercase_expanded_terms``` defaults to true) but no further
analysis is done, mainly because it is impossible to accurately
analyze a word that is missing some of its letters. However, by
setting analyze_wildcard to true, an attempt will be made to analyze
wildcarded words before searching the term list for matching terms.
Regular expression

Regular expression patterns can be embedded in the query string by
wrapping them in forward-slashes

```
("/"):
```

```
name:/joh?n(ath[oa]n)/
```

The supported regular expression syntax is explained in Regular expression syntax.
Warning

The allow_leading_wildcard parameter does not have any control over
regular expressions. A query string such as the following would force
Elasticsearch to visit every term in the index:

```
/.*n/
```

Use with caution!

#### Fuzziness

We can search for terms that are similar to, but not exactly like our
search terms, using the “fuzzy” operator:

```
quikc~ brwn~ foks~
```

This uses the Damerau-Levenshtein distance to find all terms with a
maximum of two changes, where a change is the insertion, deletion or
substitution of a single character, or transposition of two adjacent
characters.

The default edit distance is 2, but an edit distance of 1 should be
sufficient to catch 80% of all human misspellings. It can be specified
as:

```
quikc~1
```

#### Proximity searches

While a phrase query (eg "john smith") expects all of the terms in
exactly the same order, a proximity query allows the specified words
to be further apart or in a different order. In the same way that
fuzzy queries can specify a maximum edit distance for characters in a
word, a proximity search allows us to specify a maximum edit distance
of words in a phrase:

```
"fox quick"~5
```

The closer the text in a field is to the original order specified in
the query string, the more relevant that document is considered to
be. When compared to the above example query, the phrase "quick fox"
would be considered more relevant than "quick brown fox".

#### Ranges

Ranges can be specified for date, numeric or string fields. Inclusive
ranges are specified with square brackets [min TO max] and exclusive
ranges with curly brackets {min TO max}.

Properties with 1-3 bedrooms

```
bedrooms:[1 TO 3]
```

Ranges with one side unbounded can use the following syntax:

    bedrooms:>10
    bedrooms:>=10
    bedrooms:<10
    bedrooms:<=10

Note

To combine an upper and lower bound with the simplified syntax, you
would need to join two clauses with an AND operator:


    bedrooms:(>=10 AND < 20)
    bedrooms:(+>=10 +<20)

#### Boosting

Use the boost operator ^ to make one term more relevant than
another. For instance, if we want to find all documents about foxes,
but we are especially interested in quick foxes:

```
quick^2 fox
```

The default boost value is 1, but can be any positive floating point
number. Boosts between 0 and 1 reduce relevance.

Boosts can also be applied to phrases or to groups:

```
"john smith"^2   (foo bar)^4
```

#### Boolean operators

By default, all terms are optional, as long as one term matches. A
search for foo bar baz will find any document that contains one or
more of foo or bar or baz. We have already discussed the
default_operator above which allows you to force all terms to be
required, but there are also boolean operators which can be used in
the query string itself to provide more control.

The preferred operators are + (this term must be present) and - (this
term must not be present). All other terms are optional. For example,
this query:

```
quick brown +fox -news
```

states that:

 * fox must be present
 * news must not be present
 * quick and brown are optional - their presence increases the relevance

The familiar operators AND, OR and NOT (also written &&, || and !) are
also supported. However, the effects of these operators can be more
complicated than is obvious at first glance. NOT takes precedence over
AND, which takes precedence over OR. While the + and - only affect the
term to the right of the operator, AND and OR can affect the terms to
the left and right.

#### Grouping

Multiple terms or clauses can be grouped together with parentheses, to
form sub-queries:

```
(quick OR brown) AND fox
```

Groups can be used to target a particular field, or to boost the
result of a sub-query:

```
property_type:(flat OR house) title:(full text search)^2
```

#### Reserved character

If you need to use any of the characters which function as operators
in your query itself (and not as operators), then you should escape
them with a leading backslash. For instance, to search for ```(1+1)=2```,
you would need to write your query as ```\(1\+1\)=2```.

The reserved characters are: ```+ - && || ! ( ) { } [ ] ^ " ~ * ? : \ /```

Failing to escape these special characters correctly could lead to a
syntax error which prevents your query from running.

#### Watch this space

A space may also be a reserved character. For instance, if you have a
synonym list which converts "wi fi" to "wifi", a query_string search
for "wi fi" would fail. The query string parser would interpret your
query as a search for "wi OR fi", while the token stored in your index
is actually "wifi". Escaping the space will protect it from being
touched by the query string parser: "wi\ fi".

### Measurements Validation

Validation rules applied to data are:

1. Median calculation: batch job that calculates median for instant measurements (using frequencies) and cumulative measurements (using difference series that are calculated and stored for a synthetic sensor with the same type but marked accordingly, e.g. electricityConsumption_differenceSeries). Median is re-calculated every time measurements have been re-entered. This shows a ‘normal’ value for that sensor.
2. Measurement value checking: Measurements that come in N/A or as non-numeric are marked as invalid
3. Finding readings that are 200x median: a batch job that compares measurements against median of a sensor. If measurement is 200x median, it is labelled as a spike and omitted from calculations. If measurement has been replaced with a new value (ie corrected), that value is re-checked against the median and the metadata will be updated accordingly.
4. Labelling sensors with more than 10% of invalid measurements (out of range, invalid type, etc) as broken: batch job that checks last day worth of data. If the percentage of invalid measurements exceeds 10%, sensor is labelled as broken. Job runs every day so if the sensor is repaired/replaced and correct measurements are being inserted, sensor’s state is marked as ok. Invalid measurements are marked as such and are omitted from calculations. Invalid measurements are typically messages coming from sensors about invalid readings.
5. Finding mislabelled cumulative and pulse sensors: a batch job that checks if measurements are incrementing when ordered by timestamp (cumulative) or are positive integers (pulse). Sensors that don’t meet the criteria are marked as mislabelled. If corrected measurements have been entered, batch job re-checks them and updates the metadata.
6. Checking resolution of a sensor: batch job that updates resolution
   of a sensor if it is missing or if it is incorrect. It looks at 100
   last measurements and infers correct resolution from the
   timestamps.

### Conversion Factors for Calculated Datasets

There are batch jobs running that convert m3 and ft3 of gas and oil to
kWh, and kWh to CO2. If the device has any of the reading types listed
below the job automatically creates a new reading type,
e.g. oilConsumption -> oilConsumption_kWh and calculates measurements for that new
sensor type (original measurements remain unchanged).

The conversion factors are from SAP 2009.

Conversions to kWh:
* *gas consumption m3*

 kWh = m3*10.97222

* *gas consumption ft3*

 m3 = ft3*2.83

 kWh = m3*10.97222

* *oil consumption m3*

 kWh = m3*10308.34

* *oil consumption ft3*

  m3 = ft3*2.83

  kWh = m3*10308.34

Conversions to CO2:

* *electricity:* 0.517

* *gas:* 0.185

* *oil:* 0.246

### API Usage for Calculated Datasets
Dataset API supports four operations: sum, divide, subtract and
multiply. This allows to incrementally build various types of
datasets, e.g. We can create a dataset that is a sum of two sensors:
electricityConsumption-fe5ab5bf19a7265276ffe90e4c0050037de923454 and
electricityConsumption-3aae0fe7ddaa5e400a2cf9580a5e548d9255c45, where
each sensor is "readingType-device_id" and name it
"electricityConsumptionSum":

```
curl -v -H 'Content-Type: application/json' -u
username:password -X POST --data
"{\"entity_id\":\"9ac7dff5635832d843dda594f58525239263ffdd37\",\"operation\":\"sum\",
\"name\": \"electricityConsumptionSum\", \"members\":
[\"electricityConsumption-fe5ab5bf19a7265276ffe90e4c0050037de923454\", \"electricityConsumption-3aae0fe7ddaa5e400a2cf9580a5e548d9255c45\"]}"
http://127.0.0.1:8010/4/entities/9ac7dff5635832d843dda594f58525239263ffdd37/datasets/
```

Then we can use that dataset to subtract
it from another reading type or dataset:

```
curl -v -H 'Content-Type: application/json' -u username:password -X POST --data "{\"entity_id\":\9ac7dff5635832d843dda594f58525239263ffdd37\",\"operation\":\"subtract\", \"name\": \"electricitySubtraction\", \"members\": [\"interpolatedElectricityConsumption-268e93a5249c24482ac1519b77f6a45f36a6231d\", \"electricityConsumptionSum-e0b523bb794402413e774cde1faf07566178a480\"]}" http://127.0.0.1:8010/4/entities/9ac7dff5635832d843dda594f58525239263ffdd37/datasets/
```
Batch job will pick up newly created datasets and will perform
calculations.

Dataset representation accepted:

```
 {"entity_id" string (required)
  "operation" string (required, one of: sum, divide, subtract)
  "name" string (required)
  "members" [string, ...]}) (required, in a form
 of "type-device_id")
```

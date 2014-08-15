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

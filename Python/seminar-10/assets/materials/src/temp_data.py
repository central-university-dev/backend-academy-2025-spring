import time


class Temperature(object):
    def __init__(self, city, reading, unit, timestamp):
        self.city = city
        self.reading = reading
        self.unit = unit
        self.timestamp = timestamp


schema_str = """{
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "title": "Temperature",
    "description": "Temperature sensor reading",
    "type": "object",
    "properties": {
      "city": {
        "description": "City name",
        "type": "string"
      },
      "reading": {
        "description": "Current temperature reading",
        "type": "number"
      },
      "unit": {
        "description": "Temperature unit (C/F)",
        "type": "string"
      },
      "timestamp": {
        "description": "Time of reading in ms since epoch",
        "type": "number"
      }
    }
  }"""


temperature_data = [
    Temperature('Rostov', 32, 'C', round(time.time() * 1000)),
    Temperature('Minsk', 24, 'C', round(time.time() * 1000)),
    Temperature('Moscow', 26, 'C', round(time.time() * 1000)),
    Temperature('Sochi', 28, 'C', round(time.time() * 1000)),
    Temperature('Tokio', 15, 'C', round(time.time() * 1000)),
    Temperature('London', 12, 'C', round(time.time() * 1000)),
    Temperature('Chicago', 63, 'F', round(time.time() * 1000)),
    Temperature('Berlin', 14, 'C', round(time.time() * 1000)),
    Temperature('Madrid', 18, 'C', round(time.time() * 1000)),
    Temperature('Phoenix', 78, 'F', round(time.time() * 1000)),
]

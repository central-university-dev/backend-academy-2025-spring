from copy import deepcopy


def delivery_reporter(err, _):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print(f'Message delivery failed: {err}')


DEFAULT_TOPIC = 'tink_backend_academy'
TEMP_TOPIC = 'tink_backend_temp'
AVRO_TOPIC = 'tink_backend_avro'
SECURITY_PROTOCOL = 'SASL_PLAINTEXT'
SASL_MECHANISM = 'PLAIN'
KAFKA_CONFIG = {
    'bootstrap.servers': 'localhost:9092',
    'security.protocol': SECURITY_PROTOCOL,
    'sasl.mechanism': SASL_MECHANISM,
    'sasl.username': 'kafka_usr',
    'sasl.password': 'kafka_passwd',
}

PRODUCER_CONFIG = deepcopy(KAFKA_CONFIG)
PRODUCER_CONFIG.update({
    'on_delivery': delivery_reporter,
})

CONSUMER_CONFIG = deepcopy(KAFKA_CONFIG)
CONSUMER_CONFIG.update({
    'session.timeout.ms': 60000,
    'auto.offset.reset': 'earliest',
    'client.id': 'tink',
    'group.id': 'tink_seminar',
})

SR_CONFIG = {
    'url': 'http://localhost:8085',
}


# Необязательный обратный вызов по доставке для каждого сообщения (вызывается poll() или flush())
# когда сообщение успешно доставлено или окончательно не удалось доставить (после повторных попыток).
def delivery_callback(err, msg):
    if err:
        print(f'ERROR: Message failed delivery: {err}')
    else:
        key = msg.key().decode('utf-8')
        value = msg.value().decode('utf-8')
        print(f'Produced event to topic `{msg.topic()}`: key = {key:12} value = {value:12}')

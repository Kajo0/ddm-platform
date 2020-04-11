#!/usr/bin/python
import configparser
import json
import pprint
import requests
import sys
import time

lastExecFile = './last_exec.properties'
baseUrl = 'http://localhost:7000'
api = {
    'algorithm': {
        'broadcast': '/coordinator/command/algorithm/broadcast/instance/{instanceId}/{algorithmId}',
        'info': '/coordinator/command/algorithm/info',
        'load': '/coordinator/command/algorithm/load'
    },
    'data': {
        'info': '/coordinator/command/data/info',
        'load': '/coordinator/command/data/load/file',
        'loadUri': '/coordinator/command/data/load/uri',
        'scatter': '/coordinator/command/data/scatter/instance/{instanceId}/{strategy}/{dataId}',
    },
    'execution': {
        'start': '/coordinator/command/execution/start/{instanceId}/{algorithmId}/{dataId}',
        'status': '/coordinator/command/execution/status/executionId}',
        'stop': '/coordinator/command/execution/stop/{executionId}'
    },
    'instance': {
        'create': '/coordinator/command/instance/create/{workers}',
        'destroy': '/coordinator/command/instance/destroy/{instanceId}',
        'destroyAll': '/coordinator/command/instance/destroy/all',
        'info': '/coordinator/command/instance/info',
        'addresses': '/coordinator/command/instance/info/{instanceId}'
    }
}


def loadJar(path):
    print("loadJar path='{}'".format(path))
    url = baseUrl + api['algorithm']['load']
    with open(path, 'rb') as file:
        algorithmId = requests.post(url,
                                    files={'file': (file.name, file, 'application/x-java-archive')}
                                    ).text
        print('  algorithmId: ' + algorithmId)
        return algorithmId


def algorithmInfo():
    print('algorithmInfo')
    url = baseUrl + api['algorithm']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def broadcastJar(instanceId, algorithmId):
    print("broadcastJar instanceId='{}' algorithmId='{}'".format(instanceId, algorithmId))
    url = baseUrl + api['algorithm']['broadcast'].format(**{
        'instanceId': instanceId,
        'algorithmId': algorithmId
    })
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def dataInfo():
    print('dataInfo')
    url = baseUrl + api['data']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def loadData(path, label, separator=',', id=None):
    print("loadData path='{}' id='{}' label='{}' separator='{}'".format(path, id, label, separator))
    url = baseUrl + api['data']['load']
    with open(path, 'rb') as file:
        dataId = requests.post(url,
                               files={'dataFile': (file.name, file, 'application/x-java-archive')},
                               data={
                                   'idIndex': id,
                                   'labelIndex': label,
                                   'separator': separator
                               }
                               ).text
        print('  dataId: ' + dataId)
        return dataId


def scatterData(instanceId, dataId, strategy='uniform'):
    print("scatterData instanceId='{}' dataId='{}' strategy='{}'".format(instanceId, dataId, strategy))
    url = baseUrl + api['data']['scatter'].format(**{
        'instanceId': instanceId,
        'dataId': dataId,
        'strategy': strategy
    })
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def startExecution(instanceId, algorithmId, dataId):
    print("startExecution instanceId='{}' algorithmId='{}' dataId='{}'".format(instanceId, algorithmId, dataId))
    url = baseUrl + api['execution']['start'].format(**{
        'instanceId': instanceId,
        'algorithmId': algorithmId,
        'dataId': dataId
    })
    executionId = requests.get(url).text
    print('  executionId: ' + executionId)
    return executionId


def createInstance(workers):
    print("createInstance workers='{}'".format(workers))
    url = baseUrl + api['instance']['create'].format(**{'workers': workers})
    instanceId = requests.get(url).text
    print('  instanceId: ' + instanceId)
    return instanceId


def instanceInfo():
    print('instanceInfo')
    url = baseUrl + api['instance']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def destroyAll():
    print('destroyAll')
    url = baseUrl + api['instance']['destroyAll']
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def saveLast(instanceId, algorithmId, dataId):
    config = configparser.RawConfigParser()
    config['last'] = {
        'instance_id': instanceId,
        'algorithm_id': algorithmId,
        'data_id': dataId
    }
    with open(lastExecFile, 'w') as file:
        config.write(file)


def loadLast():
    config = configparser.RawConfigParser()
    config.read(lastExecFile)
    return dict(config['last'])


def setupDefault(workers=2):
    algorithmId = loadJar('./samples/aoptkm.jar')
    dataId = loadData('./samples/iris.data', 4, ',', None)
    instanceId = createInstance(workers)
    time.sleep(workers * 5)
    broadcastJar(instanceId, algorithmId)
    scatterData(instanceId, dataId, 'uniform')

    saveLast(instanceId, algorithmId, dataId)


def reload():
    last = loadLast()
    instanceId = last.get('instance_id')

    algorithmId = loadJar('./samples/aoptkm.jar')
    dataId = loadData('./samples/iris.data', 4, ',', None)
    broadcastJar(instanceId, algorithmId)
    scatterData(instanceId, dataId, 'uniform')

    saveLast(instanceId, algorithmId, dataId)


def execute():
    last = loadLast()
    print(last)
    startExecution(last.get('instance_id'), last.get('algorithm_id'), last.get('data_id'))


def clear():
    destroyAll()


if len(sys.argv) < 2:
    print('  Provide command! [setup, clear, reload, execute, info [data, alg, instance]]')
    sys.exit(1)

command = sys.argv[1]
if command == 'setup':
    setupDefault()
elif command == 'clear':
    clear()
elif command == 'reload':
    reload()
elif command == 'execute':
    execute()
elif command == 'info':
    if len(sys.argv) < 3:
        print('  Provide info arg [data, alg, instance]')
        sys.exit(1)

    arg = sys.argv[2]
    if arg == 'data':
        dataInfo()
    elif arg == 'alg':
        algorithmInfo()
    elif arg == 'instance':
        instanceInfo()
    else:
        print('  Unknown info to show')
else:
    print('  Unknown command')

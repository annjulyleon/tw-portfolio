---
author: ИНФОРИОН
title: REST API Руководство пользователя
subject: Kopycat
---

# Kopycat REST API

Kopycat предоставляет три конечные точки:

- Kopycat ('/kopycat') - основной API для управления эмуляцией Kopycat;
- Registry ('/registry') - API для управления библиотекой и реестром модулей;
- Console ('/console') - API для отправки команд в консоль Kopycat.



# KopycatRestProtocol

`/kopycat` включает полезных конечных точек для управления эмуляцией, снапшотами и памятью.



## POST /kopycat/bus

Создаёт **шину** с определенным `name` и `size` для указанных в заголовке `{device_name}`. Возвращает *string* `name` созданной **шины**. 

Шина это сущность для соединения нескольких портов для обмена данными. По своей сути аналогична шинам в реальном железе.

### Header parameters

| **Header** | **Description**                                              |
| :--------- | :----------------------------------------------------------- |
| designator | имя устройства, для которого создается шина, например, `top` |

### Body parameters

| **Parameter** | **Type** | **Description**                                              |
| :------------ | :------- | :----------------------------------------------------------- |
| name          | string   | имя шины                                                     |
| size          | string   | максимальное количество доступных адресов порта (внимание это не ширина шины!) |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/bus" --header "designator: top" --header "Content-Type: application/json" --data-raw "{
    \"name\":     \"ram\",
    \"size\":     \"BUS32\"
}"
```

Примечание: см. исходный файл **ModuleBuses.kt** [в репозитории](https://github.com/inforion/kopycat/blob/master/kopycat/src/main/kotlin/ru/inforion/lab403/kopycat/cores/base/common/ModuleBuses.kt) для более подробного описания принципов работы шины.

## POST /kopycat/port

Создаёт **порт** с определенным `name`,`type` и `size` для указанного в заголовке `{device_name}`. Возвращает *строку* `name` созданного **порта**. Порты используются для соединения модулей через шину. 

### Header parameters

| **Header** | **Description**                                              |
| :--------- | :----------------------------------------------------------- |
| designator | имя устройства, для которого создается порт, например, `top` |

### Body parameters

| **Parameter** | **Type** | **Description**                                              |
| :------------ | :------- | :----------------------------------------------------------- |
| name          | string   | имя порта                                                    |
| type          | string   | тип порта: Master, Slave, Proxy или Translator               |
| size          | string   | максимальное количество доступных адресов порта (внимание это не ширина шины!) |

### Example request:



```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/port" --header "designator: top" --header "Content-Type: application/json" --data-raw "{
    \"name\": \"port\",
    \"type\": \"Master\",
    \"size\": \"BUS16\"
}"
```

Примечание: см. исходный файл **ModulePorts.kt** [в репозитории](https://github.com/inforion/kopycat/blob/master/kopycat/src/main/kotlin/ru/inforion/lab403/kopycat/cores/base/common/ModulePorts.kt) для более подробного описания принципов работы портов.

## POST /kopycat/connect

Create connection between source and destination (for example between bus and port, or between two ports.

### Header parameters

| **Header** | **Description**                                              |
| :--------- | :----------------------------------------------------------- |
| designator | имя устройства, для которого создается соединение, например, `top` |

### Body parameters

Body parameters are passed as an array.

| **Type** | **Description**                                              |
| :------- | :----------------------------------------------------------- |
| string   | источник соединения (порт или шина)                          |
| string   | назначение соединения (порт или шина). Соединение возможно между двумя портами или портом и шиной |
| ulong    | значение смещения, по которому будет присоединен текущий порт |

```json
[
  "test.ports.mem",
  "buses.mem",
  0
]
```

В этом примере соединение создается между портом и шиной. Обратите внимание, что вы должны указать модуль и тип, используя точечную нотацию. 

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/connect" --header "designator: top" --header "Content-Type: application/json" --data-raw "[
    \"test.ports.mem\",
    \"buses.mem\",
    0
]"
```

## POST /kopycat/step

Выполнить одну инструкцию процессора.

Возвращает `True` в случае успеха и `False` в противном случае. 

### Example request



```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/step"
```



## POST /kopycat/start

Запустить без блокировки выполнения программы эмуляцию.

Возвращает `True`, если в процессоре произошло исключение. 

### Example request

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/start"
```



## POST /kopycat/halt

Прерывание работы и ожидание перехода в останов.

Возвращает `True` в случае успеха и `False` в противном случае. 

### Example request

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/halt"
```

## POST /kopycat/memLoad

Загрузить массив байт размером [size] с заданного адреса [ss]:[address] с шины `mem` процессора `cpu`.

Возвращает массив загруженных байт.

### Body parameters

| **Parameter** | **Type** | **Description**                                |
| :------------ | :------- | :--------------------------------------------- |
| address       | ulong    | адрес начала загрузки                          |
| size          | int      | количество байт для загрузки                   |
| ss            | int      | дополнительная часть адреса (segment selector) |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/memLoad" --header "Content-Type: application/json" --data-raw "{
    \"address\": 134217728,
    \"size\":    8,
    \"ss\":      0
}"
```



## POST /kopycat/memStore

Сохранить массив байт [data] в заданный адрес [ss]:[address] по шине `mem` процессора `cpu`.

### Body parameters

| **Parameter** | **Type**  | **Description**                                |
| :------------ | :-------- | :--------------------------------------------- |
| address       | bigint    | адрес начала загрузки                          |
| data          | bytearray | массив байт для сохранения                     |
| ss            | int       | дополнительная часть адреса (segment selector) |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/memStore" --header "Content-Type: application/json" --data-raw "{
    \"address\":  134217728,
    \"data\":     \"00000000bbaa053c0001a534ffff001000000000\",
    \"ss\":       0
}"
```



## POST /kopycat/regRead

Прочитать значение регистра процессора `cpu` с именем `[name]`.

Возвращает прочитанное значение регистра.

### Body parameters

| **Parameter** | **Type** | **Description**         |
| :------------ | :------- | :---------------------- |
| index         | string   | имя регистра для чтения |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/regRead" --header "Content-Type: application/json" --data-raw "{
    \"index\": 1
}"
```



## POST /kopycat/regWrite

Записать значение регистра процессора `cpu` с именем `[name]`.

### Body parameters

| **Parameter** | **Type** | **Description**              |
| :------------ | :------- | :--------------------------- |
| index         | string   | имя регистра для записи      |
| value         | ulong    | значение регистра для записи |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/regWrite" --header "Content-Type: application/json" --data-raw "{
    \"index\": 1,
    \"value\": 12
}"
```

## POST /kopycat/pcRead

Прочитать значение регистра счетчика команд `pc` процессора `cpu`.

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/pcRead" --data-raw "
```

## POST /kopycat/pcWrite

Записать значение регистра счетчика команд `pc` процессора `cpu`.

### Body parameters

| **Parameter** | **Type** | **Description**                   |
| :------------ | :------- | :-------------------------------- |
| value         | ulong    | значение регистра `pc` для записи |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/pcWrite" --header "Content-Type: application/json" --data-raw "{
    \"value\": 1000
}"
```



## POST /kopycat/save

Делает снимок текущей памяти с указанным именем. Возвращает True в случае успеха и False в противном случае. 

### Body parameters

| **Parameter** | **Type** | **Description** |
| :------------ | :------- | :-------------- |
| name          | string   | имя снапшота    |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/save" --header "Content-Type: application/json" --data-raw "{
    \"name\": \"test_snap\"
}"
```



## POST /kopycat/load

Загрузить снимок из файла с указанным именем из текущего каталога в память устройства. Возвращает True в случае успеха и False в противном случае. 

### Body parameters

| **Parameter** | **Type** | **Description**    |
| :------------ | :------- | :----------------- |
| name          | string   | имя файла снапшота |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/load" --header "Content-Type: application/json" --data-raw "{
    \"name\": \"test_snap\"
}"
```



## POST /kopycat/reset

Перезагрузить текущую эмуляцию (загруженный снапшот) в начальное состояние.

Возвращает `True` в случае успеха и `False` в противном случае. 

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/reset"
```



## POST /kopycat/close

Закрывает текущее эмулируемое устройство (т. е. останавливает сервер GDB, если он есть), завершает выполнение компонента (и всех дочерних компонентов) и остановливает сериализатор). 

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/close"
```

## POST /kopycat/exit

Останавливает эмуляцию (выполняет `kc.close()`) и завершает выполнение Kopycat.

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/exit"
```

## POST /kopycat/open

Открывает модуль верхнего уровня с указанным именем и рядом параметров.

### Body parameters

| **Parameter**  | **Type** | **Description**                                  |
| :------------- | :------- | :----------------------------------------------- |
| top            | string   | имя модуля верхнего уровня                       |
| gdbPort        | int      | порт для GDB сервера                             |
| gdbBinaryProto | boolean  | `true` для бинарного режима передачи данных      |
| traceable      | boolean  | `true` для включения трейсера для данного модуля |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/open" --header "Content-Type: application/json" --data-raw "{
    \"top\":             \"top\",
    \"gdbPort\":          5556,
    \"gdbBinaryProto\":   false,
    \"traceable\":        false
}"
```



## GET /kopycat/isRunning

Проверить состояние эмуляции.

Возвращает `true`, если эмуляция запущена.

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/isRunning"
```



## GET /kopycat/isTopModulePresented

Проверяет, создано ли и готово ли устройство для эмуляции. Возвращает True, если верхний модуль найден и может быть использован для эмуляции, и False в противном случае. 

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/isTopModulePresented"
```

### Example Response:

```json
false
```



## GET /kopycat/isGdbServerPresented

Проверяет, создан ли GDB-сервер для запущенного модуля.

Возвращает `true`, если GDB-сервер присутствует в модуле.

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/isGdbServerPresented"
```

### Example Response:

```json
false
```



## GET /kopycat/gdbClientProcessing

Проверяет, подключен ли к серверу GDB клиент.

Возвращает `true`, если клиент подключен.

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/gdbClientProcessing"
```

### Example Response:

```json
false
```



## GET /kopycat/getSnapshotMetaInfo

Прочитать метаинформацию об указанном снапшоте.

### Body parameters

| **Parameter** | **Type** | **Description**                                              |
| :------------ | :------- | :----------------------------------------------------------- |
| path          | string   | name of the snapshot file (if in current directory) or full path to snapshot file |

### Example Request:



```json
curl --location --request GET "http://127.0.0.1:1337/kopycat/getSnapshotMetaInfo" --header "path: test"
```



# RegistryRestProtocol

API для управления модулями, библиотеками и реестрами.



## GET /registry/getAvailableTopModules

Возвращает информацию о содержимом библиотек для верхнего устройства (модуля верхнего уровня). 

### Example request:



```bash
curl --location --request GET "http://127.0.0.1:1337/registry/getAvailableTopModules"
```



## Example response:

```json
{
    "mcu": {
        "MSP430x44x": [
            {
                "top": true,
                "parameters": []
            }
        ],
        "P2020": [
            {
                "top": true,
                "parameters": []
            }
        ],
        "stm32f042_example": [
            {
                "top": true,
                "parameters": [
                    {
                        "index": 2,
                        "name": "firmware",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "tty1",
                        "type": "String",
                        "optional": true,
                        "default": {}
                    },
                    {
                        "index": 4,
                        "name": "tty2",
                        "type": "String",
                        "optional": true,
                        "default": {}
                    }
                ]
            }
        ],
        "stm32f042_rhino": [
            {
                "top": true,
                "parameters": [
                    {
                        "index": 2,
                        "name": "fw_bytes",
                        "type": "byte[]",
                        "optional": false,
                        "default": {}
                    }
                ]
            },
            {
                "top": true,
                "parameters": [
                    {
                        "index": 2,
                        "name": "fw_file",
                        "type": "File",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "tty_dbg",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 4,
                        "name": "tty_bt",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    }
                ]
            },
            {
                "top": true,
                "parameters": [
                    {
                        "index": 2,
                        "name": "fw_file",
                        "type": "File",
                        "optional": false,
                        "default": {}
                    }
                ]
            },
            {
                "top": true,
                "parameters": [
                    {
                        "index": 2,
                        "name": "fw_res",
                        "type": "Resource",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "tty_dbg",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 4,
                        "name": "tty_bt",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    }
                ]
            },
            {
                "top": true,
                "parameters": [
                    {
                        "index": 2,
                        "name": "tty_dbg",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "tty_bt",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    }
                ]
            },
            {
                "top": true,
                "parameters": []
            },
            {
                "top": true,
                "parameters": [
                    {
                        "index": 2,
                        "name": "fw_bytes",
                        "type": "byte[]",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "tty_dbg",
                        "type": "String",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 4,
                        "name": "tty_bt",
                        "type": "String",
                        "o
```

## GET /registry/getAvailableAllModules

Возвращает информацию обо всех библиотеках.

### Example request:

```bash
curl --location --request GET "http://127.0.0.1:1337/registry/getAvailableAllModules"
```



## Example Response



```json
{
    "cores": {
        "ARMDebugger": [
            {
                "top": false,
                "parameters": []
            },
            {
                "top": false,
                "parameters": [
                    {
                        "index": 2,
                        "name": "endian",
                        "type": "Endian",
                        "optional": false,
                        "default": {}
                    }
                ]
            }
        ],
        "ARM1176JZS": [
            {
                "top": false,
                "parameters": [
                    {
                        "index": 2,
                        "name": "frequency",
                        "type": "long",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "ipc",
                        "type": "double",
                        "optional": false,
                        "default": {}
                    }
                ]
            }
        ],
        "ARMv6MCore": [
            {
                "top": false,
                "parameters": [
                    {
                        "index": 2,
                        "name": "frequency",
                        "type": "long",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "ipc",
                        "type": "double",
                        "optional": false,
                        "default": {}
                    }
                ]
            }
        ],
        "ARMv7Core": [
            {
                "top": false,
                "parameters": [
                    {
                        "index": 2,
                        "name": "frequency",
                        "type": "long",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "ipc",
                        "type": "double",
                        "optional": false,
                        "default": {}
                    }
                ]
            }
        ],
        "MipsDebugger": [
            {
                "top": false,
                "parameters": []
            }
        ],
        "MipsCore": [
            {
                "top": false,
                "parameters": [
                    {
                        "index": 2,
                        "name": "frequency",
                        "type": "long",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 3,
                        "name": "ipc",
                        "type": "double",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 4,
                        "name": "multiplier",
                        "type": "long",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 5,
                        "name": "PRId",
                        "type": "long",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 6,
                        "name": "PABITS",
                        "type": "int",
                        "optional": false,
                        "default": {}
                    },
                    {
                        "index": 7,
                        "name": "ArchitectureRevision",
                        "type": "int",
                        "optional": true,
                        "default": {}
                    },
                    {
                        "index": 8,
                        "name": "countOfShadowGPR",
                        "type": "int",
                        "optional": true,
                        "default": {}
                    },
                    {
                        "index": 9,
                        "name": "Config0Preset",
                        "type": "long",
                        "optional": true,
                        "default": {}
                    },
                    {
                        "index": 10,
                        "name": "Config1Preset",
                        "type": "long",
                        "optional": true,
                        "default": {}
                    },
                    {
                        "index": 11,
                        "name": "Config2Preset",
                        "type": "long",
```



## POST /registry/module?parent={parent}&name={name}

Создать модуль (или подмодуль) с указанным именем устройства. Возвращает True, если модуль успешно создан, и False в противном случае. 

### Query parameters

| **Parameter** | **Description**                                              |
| :------------ | :----------------------------------------------------------- |
| parent        | имя родительского устройства. `null` если модуль создается как родительское устройство |
| name          | имя создаваеммого устройства                                 |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/registry/module?parent=null&name=top" --header "name: top"
```

## DEL /registry/module

Удаляет модуль с указанным именем `name`.

Обратите внимание, что модуль, *открытый* (_open_) с помощью команды `/kopycat/open`, может быть закрыт только с помощью команды `/kopycat/close`. 

### Header parameters

| **Parameter** | **Description**                                  |
| :------------ | :----------------------------------------------- |
| name          | имя модуля для удаления                          |
| hierarchy     | удалять или нет дочернюю иерархию данного модуля |

**ВНИМАНИЕ!** Если для иерархии установлено значение `true`, все дочерние модули модуля `name` будут удалены! 

### Example request:

```bash
curl --location --request DELETE "http://127.0.0.1:1337/registry/module" --header "hierarchy: true" --header "name: top"
```



## POST /registry/instantiate

Инстанциировать модуль (или подмодуль) с указанным именем устройства. 

Возвращает `name` созданного устройства.

### Header parameters

| **Parameter** | **Description**                                              |
| :------------ | :----------------------------------------------------------- |
| parent        | имя родительского устройства. `null` если модуль создается как родительское устройство |

### Body parameters

| **Parameter**    | **Type** | **Description**                                              |
| :--------------- | :------- | :----------------------------------------------------------- |
| name             | string   | имя устройства или модуля                                    |
| plugin           | string   | имя плагина                                                  |
| library          | string   | имя библиотеки плагина                                       |
| params           | array    | массив параметров для инстанцируемого модуля. Список значений определяется внутри указанного класса модуля (или конфигурации json) |
| params/frequency | int      | частота в Гц                                                 |
| params/ipc       | float    | количество инструкция на циклы                               |
| params/PRid      | bigint   |                                                              |
| params/PABITS    | int      |                                                              |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/registry/instantiate" --header "parent: top" --header "Content-Type: application/json" --data-raw "{
    \"name\":     \"ram\",
    \"plugin\":   \"RAM\",
    \"library\":  \"cores\",
    \"params\":
    {
        \"frequency\": 4000000,
        \"ipc\":       0.25,
        \"PRId\":      59774906366,
        \"PABITS\":    32
    }
}"
```

Еще один пример:

```json
{
  "top": true,
  "plugin": "mipsSoc",
  "library": "srisa",
  "params": [
    {
      "name": "serialPort",
      "type": "String",
      "default": "COM10"
    },
    {
      "name": "freq",
      "type": "int"
    }
  ],
  "ports": [
    {
      "name": "mem",
      "type": "Proxy",
      "size": "BUS30"
    },
    {
      "name": "io",
      "type": "Proxy",
      "size": "16"
    }
  ],
  "buses": [
    {
      "name": "mem",
      "size": "BUS30"
    },
    {
      "name": "io",
      "size": "16"
    },
    null
  ],
  "modules": [
    {
      "name": "boot",
      "plugin": "ROM",
      "library": "memory",
      "params": {
        "size": "0*00001000",
        "data:byte[]": "00AABBCCDD"
      }
    },
    {
      "name": "firmware",
      "plugin": "ROM",
      "library": "memory",
      "params": {
        "size": "0*00002000",
        "data:File": "binaries/firmware.bin"
      }
    }
  ],
  "connections": [
    [
      "rom.ports.mem",
      "buses.mem",
      "0x1FC00000"
    ],
    [
      "ram.ports.mem",
      "buses.mem",
      "0x20000000"
    ],
    null
  ]
}
```



# ConsoleRestProtocol

## GET /console/name

Возвращает`string` с текущим типом консоли запущенного эмулятора (Python или Kotlin).

### Example Request

```bash
curl --location --request GET "http://127.0.0.1:6666/console/name"
```

### Example Response

```json
"Kotlin"
```

## POST /console/eval

Передать выражение в консоль Kopycat в виде обычного текста и, если выполнение было успешным, вернуть (0), а если не успешным - (-1).

### Example Request



```bash
curl --location --request POST "http://127.0.0.1:6666/console/eval" --header "Content-Type: text/plain" --data-raw "println(\"hi\")"
```



## POST /console/execute

Выполнить переданное выражение и вернуть результат выполнения в виде текста.

### Example Request

```bash
curl --location --request POST "http://127.0.0.1:6666/console/execute" --data-raw "println(\"hi\")"
```


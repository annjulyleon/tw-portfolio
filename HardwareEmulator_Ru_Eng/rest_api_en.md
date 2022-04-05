---
author: LLC INFORION
title: REST API Reference
subject: Kopycat
---



# Kopycat REST API

Kopycat provide three API endpoints:

- Kopycat ('/kopycat') - main API to manage emulation
- Registry ('/registry') - API to manage libraries and register of modules
- Console ('/console') - sends command to Kopycat console.

Please, note, that current parrent collection (named Kopycat) has number of declared variables (see tab **Variables):**

- `PORT` - default `1337`. Point to current instance of kopycat with specified port
- `BASE_URL` - default `http://127.0.0.1`. Base URL for endpoints and requests
- `DEVICE_NAME` - default `top`. Name of the created device



# KopycatRestProtocol

`/kopycat` include number of usefull endpoints to manage current emulation, snapshots and memory.



## POST /kopycat/bus

Create **bus** with specific `name` and `size` for specified in header `{device_name}`. Returns *string* `name` of the created **bus**.

**Bus** is an entity used to connect multiple ports to exchange data.

### Header parameters

| **Header** | **Description**                       |
| :--------- | :------------------------------------ |
| designator | name of the device, for example `top` |

### Body parameters

| **Parameter** | **Type** | **Description**                                              |
| :------------ | :------- | :----------------------------------------------------------- |
| name          | string   | name of the bus                                              |
| size          | string   | max available port addresses (BUS16, BUS32, BUS64) (note, this is not *bus width)* |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/bus" --header "designator: top" --header "Content-Type: application/json" --data-raw "{
    \"name\":     \"ram\",
    \"size\":     \"BUS32\"
}"
```

Note: see source file **ModuleBuses.kt** [in repository](https://github.com/inforion/kopycat/blob/master/kopycat/src/main/kotlin/ru/inforion/lab403/kopycat/ cores/base/common/ModuleBuses.kt) for a more detailed description of bus operation. 

## POST /kopycat/port

Create **port** with specific `name`,`type` and `size` for specified in header `{device_name}`. Returns *string* `name` of the created **port**.

Ports are used to connect modules though bus.

### Header parameters

| **Header** | **Description**                        |
| :--------- | :------------------------------------- |
| designator | name of the device, for example `port` |

### Body parameters

| **Parameter** | **Type** | **Description**                                            |
| :------------ | :------- | :--------------------------------------------------------- |
| name          | string   | name of the port                                           |
| type          | string   | Master, Slave, Proxy or Translator                         |
| size          | string   | max available port addresses (format: BUS16, BUS32, BUS64) |

### Example request:



```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/port" --header "designator: top" --header "Content-Type: application/json" --data-raw "{
    \"name\": \"port\",
    \"type\": \"Master\",
    \"size\": \"BUS16\"
}"
```

Note: see source file **ModulePorts.kt** [in repository](https://github.com/inforion/kopycat/blob/master/kopycat/src/main/kotlin/ru/inforion/lab403/kopycat/cores/base/common/ModulePorts.kt) for a more detailed description. 

## POST /kopycat/connect

Create connection between source and destination (for example between bus and port, or between two ports.

### Header parameters

| **Header** | **Description**                       |
| :--------- | :------------------------------------ |
| designator | name of the device, for example `top` |

### Body parameters

Body parameters are passed as an array.

| **Type** | **Description**           |
| :------- | :------------------------ |
| string   | source of connection      |
| string   | destination of connection |
| ulong    | offset                    |

```json
[
  "test.ports.mem",
  "buses.mem",
  0
]
```

In this example connection is created between Port and Bus. Note, that you must specify module and type using dot notation.

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/connect" --header "designator: top" --header "Content-Type: application/json" --data-raw "[
    \"test.ports.mem\",
    \"buses.mem\",
    0
]"
```

## POST /kopycat/step

Execute a single cpu instruction.

Returns `True` if successful, and `False` otherwise.

### Example request



```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/step"
```



## POST /kopycat/start

Run emulation without blocking program execution.

Returns `True` if an exception occurred in the processor.

### Example request

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/start"
```



## POST /kopycat/halt

Interrupt and wait for a breakpoint.

Returns `True` if successful, and `False` otherwise.

### Example request

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/halt"
```

## POST /kopycat/memLoad

Load an array of bytes of size `size` from the given address `ss:address` from the `mem` bus of the `cpu` processor.

Returns `string` byte array.

### Body parameters

| **Parameter** | **Type** | **Description**                               |
| :------------ | :------- | :-------------------------------------------- |
| address       | ulong    | starting address for load                     |
| size          | int      | ammount of bytes to load                      |
| ss            | int      | additional part of address (segment selector) |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/memLoad" --header "Content-Type: application/json" --data-raw "{
    \"address\": 134217728,
    \"size\":    8,
    \"ss\":      0
}"
```



## POST /kopycat/memStore

Save byte array `data` to given address `ss:address` on bus `mem` of processor `cpu`.

### Body parameters

| **Parameter** | **Type**  | **Description**                               |
| :------------ | :-------- | :-------------------------------------------- |
| address       | bigint    | starting address for load                     |
| data          | bytearray | byte array of data to load                    |
| ss            | int       | additional part of address (segment selector) |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/memStore" --header "Content-Type: application/json" --data-raw "{
    \"address\":  134217728,
    \"data\":     \"00000000bbaa053c0001a534ffff001000000000\",
    \"ss\":       0
}"
```



## POST /kopycat/regRead

Read the value of the processor register `cpu` with index `index`.

Return `ULong` stored value of the register.

### Body parameters

| **Parameter** | **Type** | **Description**               |
| :------------ | :------- | :---------------------------- |
| index         | string   | index of the register to read |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/regRead" --header "Content-Type: application/json" --data-raw "{
    \"index\": 1
}"
```



## POST /kopycat/regWrite

Write the value of the processor register `cpu` with index `index`.

### Body parameters

| **Parameter** | **Type** | **Description**              |
| :------------ | :------- | :--------------------------- |
| index         | string   | index of the register        |
| value         | ulong    | value to write into register |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/regWrite" --header "Content-Type: application/json" --data-raw "{
    \"index\": 1,
    \"value\": 12
}"
```

## POST /kopycat/pcRead

Read the `ULong` value of the program counter register `pc` of the processor `cpu`.

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/pcRead" --data-raw "
```

## POST /kopycat/pcWrite

Write the value of the program counter register `pc` of the processor `cpu`.

### Body parameters

| **Parameter** | **Type** | **Description**                                |
| :------------ | :------- | :--------------------------------------------- |
| value         | ulong    | value of the program counter register to write |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/pcWrite" --header "Content-Type: application/json" --data-raw "{
    \"value\": 1000
}"
```



## POST /kopycat/save

Makes snapshot of current memory with specified `name`.

Returns `True` if successful, and `False` otherwise.

### Body parameters

| **Parameter** | **Type** | **Description**           |
| :------------ | :------- | :------------------------ |
| name          | string   | name of the snapshot file |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/save" --header "Content-Type: application/json" --data-raw "{
    \"name\": \"test_snap\"
}"
```



## POST /kopycat/load

Load snapshot from file with specified `name` from current directory to device memory.

Returns `True` if successful, and `False` otherwise.

### Body parameters

| **Parameter** | **Type** | **Description**                   |
| :------------ | :------- | :-------------------------------- |
| name          | string   | name of the snapshot file to load |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/load" --header "Content-Type: application/json" --data-raw "{
    \"name\": \"test_snap\"
}"
```



## POST /kopycat/reset

Reset loaded snapshot to initial state.

Returns `True` if successful, and `False` otherwise.

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/reset"
```



## POST /kopycat/close

Close current emulated device (i.e. stop GDB server, if present, terminate component execution (and all children components), and stop serializer).

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/close"
```

## POST /kopycat/exit

Stop emulation (execute `kc.close()`) and terminate kopycat execution.

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/kopycat/exit"
```

## POST /kopycat/open

Open specified top module with name `top`.

### Body parameters

| **Parameter**  | **Type** | **Description**                                              |
| :------------- | :------- | :----------------------------------------------------------- |
| top            | string   | name of the top module                                       |
| gdbPort        | int      | port for gdb connection                                      |
| gdbBinaryProto | boolean  | `true` for binary data mode, `false` - default mode          |
| traceable      | boolean  | `true` to set specified top module to be traceable, `false` - not traceable |

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

Get state of Kopycat emulation.

Returns `True` if running. and `False` otherwise.

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/isRunning"
```



## GET /kopycat/isTopModulePresented

Check if the device for emulation has been created and ready.

Returns `True` if top module is found and can be used for emulation, and `False` otherwise.

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/isTopModulePresented"
```

### Example Response:

```json
false
```



## GET /kopycat/isGdbServerPresented

Check if gdb server is present.

Returns `True` if gdb is present, and `False` otherwise.

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/isGdbServerPresented"
```

### Example Response:

```json
false
```



## GET /kopycat/gdbClientProcessing

Check whether the client is connected to the GDB server.

Returns `True` if client is connected to GDB server, and `False` otherwise.

### Example Request:

```bash
curl --location --request GET "http://127.0.0.1:1337/kopycat/gdbClientProcessing"
```

### Example Response:

```json
false
```



## GET /kopycat/getSnapshotMetaInfo

Get snapshot meta information.

### Body parameters

| **Parameter** | **Type** | **Description**                                              |
| :------------ | :------- | :----------------------------------------------------------- |
| path          | string   | name of the snapshot file (if in current directory) or full path to snapshot file |

### Example Request:



```json
curl --location --request GET "http://127.0.0.1:1337/kopycat/getSnapshotMetaInfo" --header "path: test"
```



# RegistryRestProtocol



## GET /registry/getAvailableTopModules

Returns information about libraries content for top device.

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

Returns information about all libraries content.

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

Create module (or sub-module) with specified device name.

Returns `True` if module is successfully created, and `False` otherwise.

### Query parameters

| **Parameter** | **Description**                                              |
| :------------ | :----------------------------------------------------------- |
| parent        | name of parent device if any. `null` module is created as top device |
| name          | name of created device                                       |

### Example request:

```bash
curl --location --request POST "http://127.0.0.1:1337/registry/module?parent=null&name=top" --header "name: top"
```

## DEL /registry/module

Delete module with specified `name`.

Please note, module *opened* via `/kopycat/open` command can only be closed via `/kopycat/close` command.

### Header parameters

| **Parameter** | **Description**          |
| :------------ | :----------------------- |
| name          | name of module to delete |
| hierarchy     | true                     |

**ATTENTION!** If hierarchy is set to `True`, all child modules of module `name` will be deleted!

### Example request:

```bash
curl --location --request DELETE "http://127.0.0.1:1337/registry/module" --header "hierarchy: true" --header "name: top"
```



## POST /registry/instantiate

Create module (or sub-module) with specified device name.

Returns `name` of created component.

### Header parameters

| **Parameter** | **Description**                                              |
| :------------ | :----------------------------------------------------------- |
| parent        | name of parent device if any. `null` module is created as top device |

### Body parameters

| **Parameter**    | **Type** | **Description**                                              |
| :--------------- | :------- | :----------------------------------------------------------- |
| name             | string   | name of the device or component                              |
| plugin           | string   | name of the plugin                                           |
| library          | string   | library of the plugin                                        |
| params           | array    | array of parameters for instantiated module. List of values is defined insinde specified module class (or json configuration) |
| params/frequency | int      | frequency in Hz                                              |
| params/ipc       | float    | instruction per cycles                                       |
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

Another example:

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

Console REST API can send commands directly to Kopycat console.

## GET /console/name

Returns `string` with current console type (Python or Kotlin).

### Example Request

```bash
curl --location --request GET "http://127.0.0.1:6666/console/name"
```

### Example Response

```json
"Kotlin"
```

## POST /console/eval

Pass expression to Kopycat console as plaint text and returns if execution was successfull (0) or not (-1).

### Example Request



```bash
curl --location --request POST "http://127.0.0.1:6666/console/eval" --header "Content-Type: text/plain" --data-raw "println(\"hi\")"
```



## POST /console/execute

Execute statement and get back result of execution.

### Example Request

```bash
curl --location --request POST "http://127.0.0.1:6666/console/execute" --data-raw "println(\"hi\")"
```


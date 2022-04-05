---
author: ИНФОРИОН
title: Руководство пользователя
subject: Kopycat
comments: Документация, содержащая информацию, необходимую для эксплуатации экземпляра программного обеспечения, предоставленного для проведения экспертной проверки. Инструкция по установке экземпляра программного обеспечения, предоставленного для проведения экспертной проверки
---



# Введение

**Kopycat** - программный эмулятор аппаратных систем. **Kopycat** имеет модульную архитектуру. Основные компоненты:

- конфигурация с описанием модулей и связей между ними;
- набор модулей, которые описывают работу отдельных компонентов устройства (в формате jar/json). Kopycat формирует платформу для эмуляции из отдельных модулей. При этом модули организованы иерархически и всегда есть основной модуль или *модуль верхнего уровня* (`top`), который включает внутренние модули и описывает связи между ними;
- данные ПЗУ устройства;
- снапшот (снимок состояния эмулятора).

# Подготовка среды исполнения

Kopycat предоставляется в виде готового дистрибутива и исходного кода для самостоятельной сборки.

Для запуска дистрибутива на компьютере должно быть установлено:

* OpenJDK (проверена работа с версиями 11.0.6, 11.0.7);
* для встроенной консоли REPL Python - Python (версии 2.7, 3.6, 3.8) с пакетом Jep (0.3.9). Готовые сборки OpenJDK можно скачать по ссылке: https://adoptopenjdk.net/. Для установки необходимых зависимостей пользователи ОС Linux и OSX могут использовать стандартный менеджер пакетов (pip или иной). Перед сборкой jep необходимо также установить компилятор `compiler` и `toolchain`: `gcc` для Linux и *Developer Tools* с `XCode` на OCX;
* библиотеки `unicorn`: https://www.unicorn-engine.org/;
* keystone: https://docs.openstack.org/keystone/latest/;
* nasm: https://www.nasm.us/;
* man-db: http://man-db.nongnu.org/;
* socat: https://linux-notes.org/ustanovka-socat-v-unix-linux/.

Помимо данного руководства пользователи могут посмотреть подготовленные обучающие видео, демонстрирующие процесс установки, запуска, разработки прошивки и разработки модулей для Kopycat:

* Установка и запуск Kopycat на Linux: https://youtu.be/lM2AWJG_ck4
* Использование снапшота для запуска гостевой системы: https://youtu.be/Q4rXs9GF8BQ
* Пример разработки прошивки для STM32 в CLion с Kopycat: https://youtu.be/GN-uI5s1_iU
* Запуск из Intellij и выполнение `rm- -rf` : https://youtu.be/KYMhrf2QzEg

## Особенности подготовки среды ОС Windows

1. Пользователям ОС Windows необходимо вручную загрузить и установить OpenJDK, а затем прописать переменные среды `PATH` и `JAVA_HOME` (см.https://openjdk.java.net/install/ ).

2. Загрузить и установить Python (https://www.python.org/downloads/), установить менеджер пакетов, а также добавить путь к установленному Python в переменную `PATH`. 

3. Загрузить и установить средства сборки Visual Studio (build tools) версии 14.x: https://visualstudio.microsoft.com/visual-cpp-build-tools/

4. Исправить Python setuptools для работы с компилятором Visual Studio:

   1. в файле `C:\<Python dir>\Lib\distutils\msvc9compiler.py` необходимо добавить строку:

      ```py
      ld_args.append('/MANIFEST')
      ```

5. Запустить консоль **x64 Native Tools Command Prompt** (доступно в меню Пуск) и выполнить команду:

   ```py
   pip install jep==3.9.1
   ```

## Установка зависимостей в ОС Linux

Последовательно выполнить указанные ниже команды:

```bash
# only for debian 9
echo 'deb http://ftp.debian.org/debian stretch-backports main' | sudo tee /etc/apt/sources.list.d/stretch-backports.list
sudo apt update

sudo apt install gcc openjdk-11-jdk curl python socat

# path may differ. JAVA_HOME required to be set for jep installation
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
sudo python get-pip.py
pip install jep
```



Примечание: подробности установки OpenJDK на Ubuntu см. по ссылке: https://dzone.com/articles/installing-openjdk-11-on-ubuntu-1804-for-real.

## Установка зависимостей в OSX

Последовательно выполнить указанные ниже команды:

```bash
brew install socat
brew cask install adoptopenjdk
brew install adoptopenjdk/openjdk/adoptopenjdk-openjdk11
brew install python@3.7
pip install jep
```

Примечание: подробности установки OpenJDK на OSX см. по ссылке:https://dzone.com/articles/install-openjdk-versions-on-the-mac.

## Требования для разработки модулей Kopycat

Предполагается, что разработка модулей для Kopycat и работа с исходниками ведется в Intellij (версии 2020.2 и выше) с плагином Kotlin (версии 1.4.21 и выше).

# Быстрый старт

В данном разделе приводится несколько примеров запуска эмуляции аппаратных платформ:

* для устройства на базе STM32F042 (ядро: Cortex-M0, архитектура: ARMv6M);
* виртуальное ARM устройство (ядро: ARM1176JZS, архитектура: ARMv6/v7).

Кроме того приводится пример выполнения команд `ls` и `cat` для ARM архитектуры в пользовательской ОС с модулем эмуляции гостевой системы VEOS.

Для STM32F042 реализованы следующие периферийные модули: UART, TIMx, DMAC, GPIOx, WDG. Этого достаточно для запуска FreeRTOS (пример работы с FreeRTOS приведен в прошивке `freertos_uart`, предоставляемой в комплекте с дистрибутивом). Виртуальный ARM (VirtARM) включает UBoot, Linux с ядром 2.6.x и файловую систему ext2. Во всех примерах работы ниже используется архитектура ARM, так как именно эта архитектура используется в большинстве современных устройств.

*Примечание*: У разных производителей концепция ядра и архитектура может отличаться. Например, ARM имеет сложную систему архитектуры и ядра. На самом низком уровне представлена архитектура - то есть  ARMv6, ARMv7 (процессоры, CPU), ARMv6M (MCU, встроенные микроконтроллеры) и т.д. Эти архитектуры используются в ядрах (т.е. ARM1176JZS (но не ARMv11!), CortexM0, CortexM3 и т.д.). А затем реализуется микроконтроллер - то есть STM32F04. Для TI MSP430 ядро, архитектура и сам микроконтроллер практически одно и то же. Так что мы часто будем говорить о "ядре" имя в виду ядро *или* архитектуру. Более того, в исходниках отсутствует такая сущность как "архитектура". На самом низком уровне Kopycat оперирует `Core`, которое состоит из `CPU` (собственно ЦПУ, которое исполняет инструкции), `Decoder`, `COP` (сопроцессора coprocessor, обрабатывающего прерывания), `MMU` (блок управления памятью).

## Запуск модуля STM32F402 на ядре Cortex-M0

1. Загрузить готовую сборку эмулятора `kopycat-X.Y.AB` со страницы релизов (https://github.com/inforion/kopycat/releases/latest) и распаковать в рабочую директорию. Не рекомендуется использовать директории с пробелами и специальными символами в пути.

2. Добавить переменную среды `KOPYCAT_HOME`  с ссылкой на рабочую директорию (используется Kopycat для поиска стандартной библиотеки модулей). Например, `KOPYCAT_HOME=/opt/kopycat-X.Y.Z-RCx`. Также добавить путь к директории `KOPYCAT_HOME/bin` к переменной `Path`.

3. Загрузить сборку библиотек модулей для Kopycat (архив вида `modules-X.Y.AB`) со страницы релизов (https://github.com/inforion/kopycat/releases/latest) и:
   1. распаковать архив в любую директорию и прописать путь к ней в переменной среды `KOPYCAT_MODULES_LIB_PATH`;
   2. или распаковать архив в директорию `${KOPYCAT_HOME}/modules`.
   
4. Запустить эмуляцию одного из модулей из библиотеки, например STM32F042.:

   ```bash
   kopycat -y ${KOPYCAT_MODULES_LIB_PATH} -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
   ```

   Или, если была установлена переменная среды, а модули загружены в директорию Kopycat:

   ```bash
   kopycat -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
   ```
Параметры:
* `-y` - путь к директориям с модулями (реестр - `registry`). Можно указать несколько: `-y modules/user,modules/basic`;
* `-l`- конкретная библиотека для модуля верхнего уровня (то есть имя папки в директории `registry`);
* `-n` - имя модуля (в данном случае - `stm32f042_example`), имя класса Kotlin внутри `.jar` модуля;
* `-p` - параметры для `stm32f042_example` модуля (аргументы класса). У каждого модуля могут быть свои аргументы, посмотреть их можно в исходном коде конкретного модуля. В данном случае параметры включают:
  * `firmware` - прошивка, которую необходимо загрузить в FLASH-память STM32F042 (в данном случае это просто echo):
    * `example` - для загрузки одной из прошивок из `stm32f0xx.jar`: `benchmark_qsort`, `freertos_uart`, `gpiox_led`, `gpiox_registers`, `rhino_fw42k6`, `usart_dma`, `usart_poll`. Исходный код для каждой прошивки опубликован в репозитории в папке `kopycat-modules/mcu/stm32f0xx/firmwares`;
    * `file` - путь к бинарному файлу (т.е. к файлу .bin из CLion);
    * `bytes` - шестнадцатеричная строка (hex), например, `AACCDDEE90909090`.
  * `ttyX` - виртуальный терминал, подключенный к usart1 и usart2 STM32F402;
* `g` - порт для запуска GDB сервера

*Примечания*:
* если вы создали переменную `KOPYCAT_HOME`, то модули из директории `${KOPYCAT_HOME}/modules` инициализируются автоматически (не нужно отдельно указывать параметр `-y`);
* указанная выше команда запуска будет работать только для ОС *nix из-за socat (который недоступен на Windows). Терминал можно отключить, указав в строке запуска: `tty1=null,tty2=null` Для ОС Windows используйте com0com (или любой аналог). В этом случае в команде необходимо указать имена виртуальных COM портов: `tty1=COM1,tty2=COM2`.

5. В случае успешного запуска будет выведен лог следующего вида. В конце лога будет отображено приглашение к вводу.
   Для консоли Python:

```
bat@Kernel ~ % kopycat -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
12:53:54 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
12:53:54 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '...'
12:53:55 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
12:53:55 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
12:53:55 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): stm32f042_example(null, top, firmware=example:usart_poll, tty1=socat:, tty2=socat:)
12:53:55 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [172.20.10.6:23946]
12:53:56 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term1: /dev/ttys014 and /dev/ttys015
12:53:56 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term2: /dev/ttys016 and /dev/ttys017
12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.stm32f042.cortexm0.arm for top
12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.stm32f042.dbg for top
12:53:56 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
12:53:56 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term2'
12:53:56 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term1'
12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
12:53:56 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
12:53:56 INF ...modules.cortexm0.CORTEXM0.reset(CORTEXM0.kt:58): Setup CORTEX-M0 core PC=0x080022C1 MSP=0x20001800
12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
12:53:56 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[stm32f042_example] with arm[ARMv6MCore] is ready
12:53:56 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.stm32f042.dbg for GDB_SERVER(port=23946,alive=true)
12:53:56 WRN ...at.KopycatStarter.console(KopycatStarter.kt:44): Use -kts option to enable Kotlin console. In the next version Kotlin console will be default.
12:53:56 CFG ...at.consoles.jep.JepLoader.load(JepLoader.kt:53): Loading Jep using Python command 'python3' to overwrite use '--python' option
12:53:56 CFG ...oles.jep.PythonShell.version(PythonShell.kt:34): Python Version(major=3, minor=9, micro=0)
12:53:56 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep jar file: /usr/local/lib/python3.9/site-packages/jep/jep-3.9.1.jar
12:53:56 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep shared library file: /usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so
Jep starting successfully!
12:53:56 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Python console enabled
Python > 
```

Для консоли Kotlin:
```
bat@Kernel ~ % kopycat -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:" -kts           
12:55:07 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
12:55:08 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
12:55:09 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
12:55:09 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
12:55:09 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): stm32f042_example(null, top, firmware=example:usart_poll, tty1=socat:, tty2=socat:)
12:55:09 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [172.20.10.6:23946]
12:55:09 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term1: /dev/ttys018 and /dev/ttys019
12:55:10 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term2: /dev/ttys020 and /dev/ttys021
12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.stm32f042.cortexm0.arm for top
12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.stm32f042.dbg for top
12:55:10 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
12:55:10 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term2'
12:55:10 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term1'
12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
12:55:10 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
12:55:10 INF ...modules.cortexm0.CORTEXM0.reset(CORTEXM0.kt:58): Setup CORTEX-M0 core PC=0x080022C1 MSP=0x20001800
12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
12:55:10 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[stm32f042_example] with arm[ARMv6MCore] is ready
12:55:10 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.stm32f042.dbg for GDB_SERVER(port=23946,alive=true)
warning: runtime JAR files in the classpath should have the same version. These files were found in the classpath:
    <KOPYCAT_HOME>/lib/kotlin-reflect-1.4.10.jar (version 1.4)
    <KOPYCAT_HOME>/lib/kotlin-stdlib-1.4.10.jar (version 1.4)
    <KOPYCAT_HOME>/lib/kotlin-stdlib-jdk8-1.3.71.jar (version 1.3)
    <KOPYCAT_HOME>/lib/kotlin-stdlib-jdk7-1.3.71.jar (version 1.3)
    <KOPYCAT_HOME>/lib/kotlin-script-runtime-1.4.10.jar (version 1.4)
    <KOPYCAT_HOME>/lib/kotlin-stdlib-common-1.4.10.jar (version 1.4)
warning: some runtime JAR files in the classpath have an incompatible version. Consider removing them from the classpath
12:55:13 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Kotlin console enabled
Kotlin > 
```

*Примечания*:

* сообщение `Pseudo-terminals created for top.term1: /dev/ttys002 and /dev/ttys004`:
  * `/dev/ttys002` - конечная точка для подключения эмулятора к виртуальному COM-порту USART1;
  * `/dev/ttys004` - конечная точка для подключения пользователя к виртуальному COM-порту USART1;
* сообщение `warning: runtime JAR files in the classpath should have the same version` вызвано библиотекой Javalin, поскольку она использует Kotlin 1.3.x, который конфликтует с текущей версией Kopycat Kotlin 1.4.10 (предупреждение не влияет на работу Kopycat и может быть проигнорировано);
* все примеры команд запуска ниже будут показаны для консоли Python, но для консоли Kotlin почти все команды будут аналогичны.

6. Подключите к `/dev/ttys004` COM-порт (имя может отличаться, см. Примечание выше) с помощью putty или screen:

   ```
   screen /dev/ttys004
   ```
7. В конcоли Kopycat выполните команду:
   ```
   kc.start()  # run Kopycat emulation
   ```
8. Теперь вы можете напечатать и отправить что-то в терминале `/dev/ttys004` - и увидеть `echo` в консоли Kopycat, отправленное в прошивку STM32F042. Если эмуляция не запущена, то вы не увидите в консоли Kotlin отправляемых сообщений.
9. Для завершения эмуляции выполните команду:
   ```
   kc.halt()  # run Kopycat emulation
   ```
   `kc` - специальный прокси-объект, который видим всем методам класса Kopycat. Все доступные методы объекта `kс` можно просмотреть в исходном коде класса `Kopycat`.

## Запуск модуля ARM на ядре ARM1176JZS

1. Запуск эмуляции модуля Linux 2.6.xx на ядре ARM:

   ```
   kopycat -y ${KOPYCAT_MODULES_LIB_PATH} -l mcu -n VirtARM -g 23946 -p "tty=socat:"
   
   13:29:02 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
   13:29:02 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
   13:29:03 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
   13:29:03 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
   13:29:03 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): VirtARM(null, top, tty=socat:)
   13:29:03 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [172.20.10.6:23946]
   13:29:04 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term: /dev/ttys018 and /dev/ttys019
   13:29:04 CFG ....modules.virtarm.VirtARM.<init>(VirtARM.kt:115): Setting bootloaderCmd: 'setenv machid 25f8
   setenv bootargs console=ttyS0,115200n8 ignore_loglevel root=/dev/mtdblock0 init=/linuxrc lpj=622592
   setenv verify n
   bootm 1000000
   '
   13:29:04 CFG ....modules.virtarm.VirtARM.<init>(VirtARM.kt:120): Loading GCC map-file...
   13:29:04 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.arm1176jzs for top
   13:29:04 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.dbg for top
   13:29:04 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
   13:29:04 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term'
   13:29:04 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
   13:29:04 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
   13:29:04 FNE ...ware.processors.ARMv6CPU.reset(ARMv6CPU.kt:151): pc=0x00000000 sp=0x00000000
   13:29:05 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
   13:29:05 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[VirtARM] with arm1176jzs[ARM1176JZS] is ready
   13:29:05 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.dbg for GDB_SERVER(port=23946,alive=true)
   13:29:05 WRN ...at.KopycatStarter.console(KopycatStarter.kt:44): Use -kts option to enable Kotlin console. In the next version Kotlin console will be default.
   13:29:05 CFG ...at.consoles.jep.JepLoader.load(JepLoader.kt:53): Loading Jep using Python command 'python3' to overwrite use '--python' option
   13:29:05 CFG ...oles.jep.PythonShell.version(PythonShell.kt:34): Python Version(major=3, minor=9, micro=0)
   13:29:05 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep jar file: /usr/local/lib/python3.9/site-packages/jep/jep-3.9.1.jar
   13:29:05 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep shared library file: /usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so
   Jep starting successfully!
   13:29:05 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Python console enabled
   ```
2. Подключитесь к `/dev/ttys013` с помощью putty или screen:
   ```
   screen /dev/ttys013
   ```
3. Запустите эмуляцию командой `kc.start()`. При запуске эмуляции вы увидите загрузку U-boot и Linux. После завершения загрузки, вы сможете подключиться к ОС в терминале `/dev/ttys013` с логином и паролем `root`/`toor`.
   

## Получение списка доступных модулей из реестра

Чтобы получить список всех доступных модулей в библиотеках, выполните команду:

```
kopycat -y ${KOPYCAT_MODULES_LIB_PATH} -all
```

где `${KOPYCAT_MODULES_LIB_PATH}` - путь к директории с модулями.

Пример вывода команды:

```
13:29:57 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
13:29:57 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
13:29:58 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
13:29:58 INF ...opycat.printModulesRegistryInfo(Kopycat.kt:722): 
Library 'PeripheralFactoryLibrary[veos]':
    Module: [       VirtualMemory] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
    Module: [     MIPSApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
    Module: [x86WindowsApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
    Module: [      ARMApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]

Library 'PeripheralFactoryLibrary[mcu]':
    Module: [                 SCB] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
    Module: [                 STK] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
    Module: [            CORTEXM0] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
    Module: [                NVIC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
    Module: [   stm32f042_example] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [     stm32f042_rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                EXTI] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [           STM32F042] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                 LED] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                IWDG] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [               GPIOx] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [               FLASH] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                  BT] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                DMAC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                 RCC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                TIMx] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [              SYSCFG] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                 TSC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [                 FMI] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [               TIM18] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [              USARTx] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [               rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [             VirtARM] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
    Module: [               Timer] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
    Module: [            NANDCtrl] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
    Module: [             NS16550] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
    Module: [               PL190] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]

Library 'PeripheralFactoryLibrary[cores]':
    Module: [        MipsDebugger] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/mips.jar]
    Module: [            MipsCore] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/mips.jar]
    Module: [          ARM1176JZS] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
    Module: [          ARMv6MCore] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
    Module: [           ARMv7Core] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
    Module: [         ARMDebugger] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
    Module: [         x86Debugger] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/x86.jar]
    Module: [             x86Core] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/x86.jar]

Library 'PeripheralFactoryLibrary[common]':
    Module: [             ATACTRL] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/ATACTRL.class]
    Module: [              M95160] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/M95160.class]
    Module: [                NAND] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/NAND.class]
    Module: [                 Hub] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/Hub.class]
    Module: [              i82551] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/i82551.class]
    Module: [        CompactFlash] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/CompactFlash.class]
    Module: [            Am79C972] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/Am79C972.class]
    Module: [             Signals] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/Signals.class]
    Module: [                  SD] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/SD.class]
    Module: [              EEPROM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/EEPROM.class]
    Module: [             PCIHost] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/pci/PCIHost.class]

Library 'PeripheralFactoryLibrary[memory]':
    Module: [                 ROM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/ROM.class]
    Module: [                VOID] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/VOID.class]
    Module: [           SparseRAM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/SparseRAM.class]
    Module: [                 RAM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/RAM.class]

Library 'PeripheralFactoryLibrary[terminals]':
    Module: [  UartStreamTerminal] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/terminals/UartStreamTerminal.class]
    Module: [        UartTerminal] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/terminals/UartTerminal.class]
    Module: [  UartSerialTerminal] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/terminals/UartSerialTerminal.class]
```



Чтобы получить список всех `top` модулей в библиотеках, выполните команду `kopycat -top`. Пример:

```
13:41:17 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
13:41:18 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
13:41:19 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
13:41:19 INF ...opycat.printModulesRegistryInfo(Kopycat.kt:722): 
Library 'PeripheralFactoryLibrary[veos]':
    Module: [     MIPSApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
    Module: [x86WindowsApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
    Module: [      ARMApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]

Library 'PeripheralFactoryLibrary[mcu]':
    Module: [   stm32f042_example] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [     stm32f042_rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [               rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
    Module: [             VirtARM] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
    Module: [           Testbench] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/testbench.jar]
    Module: [          MSP430x44x] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/msp430x44x.jar]
    Module: [               P2020] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/p2020.jar]
```



## Вывод справки по командам

Для вывода справки по командам Kopycat выполните `kopycat –help`.

```
13:43:06 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
usage: kopycat [-h] [-u MODULES] [-y REGISTRY] [-n NAME] [-l LIBRARY] [-s SNAPSHOT] [-p PARAMETERS] [-w SNAPSHOTS_DIR] [-g GDB_PORT] [-r REST] [-gb] [-run] [-standalone] [-all] [-top]
               [-ci] [-pw] [-python PYTHON] [-kts] [-ll LOG_LEVEL]

virtualization platform

named arguments:
  -h, --help             show this help message and exit
  -u MODULES, --modules MODULES
                         Modules libraries paths in format: lib1:path/to/lib1,lib2:path/to/lib2
  -y REGISTRY, --registry REGISTRY
                         Path to registry with libraries
  -n NAME, --name NAME   Top instance module name (with package path dot-separated)
  -l LIBRARY, --library LIBRARY
                         Top instance module library name
  -s SNAPSHOT, --snapshot SNAPSHOT
                         Snapshot file (top instance module/library can be obtained from here)
  -p PARAMETERS, --parameters PARAMETERS
                         Parameters for top module constructor in format: arg1=100,arg2=/dev/io
  -w SNAPSHOTS_DIR, --snapshots-dir SNAPSHOTS_DIR
                         Snapshots directory path (default path to store and load snapshots)
  -g GDB_PORT, --gdb-port GDB_PORT
                         GDB server port (if not specified then not started)
  -r REST, --rest REST   REST server port. If null - Commander will work
  -gb, --gdb-bin-proto   GDB server enabled binary protocol (default: false)
  -run, --run            Run emulation as soon as Kopycat ready (default: false)
  -standalone, --standalone
                         Run emulation as soon as Kopycat ready and exit when guest application stops (default: false)
  -all, --modules-registry-all-info
                         Print all loaded modules info and exit (default: false)
  -top, --modules-registry-top-info
                         Print top loaded modules info and exit (default: false)
  -ci, --connections-info
                         Print hier. top module buses connections info at startup (default: false)
  -pw, --ports-warnings  Print all ports warnings when loading Kopycat module at startup (default: false)
  -python PYTHON, --python PYTHON
                         Python interpreter command
  -kts, --kotlin-script  Set REPL to Kotlin script language (default: false)
  -ll LOG_LEVEL, --log-level LOG_LEVEL
                         Set messages minimum logging level for specified loggers in format logger0=LEVEL,logger1=LEVEL
                         Or for all loggers if no '=' was found in value just logger level, i.e. FINE
                         Available levels: ALL, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, DEBUG, TRACE, OFF
```



## Запуск в режиме эмуляции ОС с модулем VEOS

1. Для запуска в режиме пользователя (в прикладном режиме работы процессора) требуется установленная переменная среды `KOPYCAT_HOME`, а директория `KOPYCAT_HOME/bin` должна быть добавлена к переменной `PATH`.

2. Модуль VEOS должен существовать в директории `KOPYCAT_HOME/modules`.

3. Загрузите ELF-файлы для других архитектур (например из дистрибутива Debian Stretch):

   1. binutils: https://packages.debian.org/stretch/armel/binutils/download
   2. coreutils: https://packages.debian.org/stretch/armel/coreutils/download

4. Распаковать архивы в директорию и перейти в директорию с распакованными файлами.

5. Запустить бинарные файлы командой вида:

   ```
   kopycat-veos-arm bin/ls -la /usr/share
   ```
   Вывод команды:

   ```
    ... tons of log ...
       13:07:24 CFG ...at.veos.api.impl.StatAPI.stat64(StatAPI.kt:140): [0x00009ACC] stat64(path='/usr/share/doc' buf=0x100004E8) -> stat(st_dev=0, st_ino=0, st_mode=16877, st_nlink=1, st_uid=0, st_gid=0, st_rdev=0, st_size=96, st_blksize=0, st_blocks=0, st_atime=1607940014, st_mtime=1487766225, st_ctime=1487766225) in Process:1(state=Running)
       total 0
       drwxr-xr-x  1 root root   96 Feb 22  2017 doc
       drwxr-xr-x  1 root root   96 Feb 22  2017 info
       drwxr-xr-x 43 root root 1440 Feb 22  2017 locale
       drwxr-xr-x  2 root root  128 Feb 22  2017 man
       13:07:24 FST ...ab403.kopycat.veos.VEOS.preExecute(VEOS.kt:402): Application exited
   ```

Команда `kopycat-veos-arm` - это скрипт, расположенный в директории `<KOPYCAT_HOME>/bin`. Этот скрипт запускает Kopycat в standalobe режиме, в котором эмулятор не ждет подключения дебаггера, а сразу запускает эмулированный процессор. Для конфигурации Kopycat c модулем VEOS доступны следующие параметры среды:

- `KOPYCAT_VEOS_GDB_PORT` - запускает GDB сервер на указанному порту (если не указано - GDB сервер не запускается).
- `KOPYCAT_VEOS_CONSOLE` - тип консоли: `kotlin` или `python=<PYTHON_COMMAND>` (по умолчанию: `python=python`).
- `KOPYCAT_VEOS_WORKING_DIR` - рабочая корневая директория для эмуляции (т.е. директория директория, которая в эмулированной системе будет root'ом (по умолчанию: текущая директория).
- `KOPYCAT_VEOS_STANDALONE` - если `NO`, то процессор не будет запущен как только он готов (по умолчанию: standalone режим)
- `KOPYCAT_VEOS_LD_PRELOAD` - список предзагруженных динамических библиотек, если они требуются для ELF-файлов и не разрешаются автоматически, список через запятую (по умолчанию: пустой)
- `KOPYCAT_VEOS_LOGGING_CONF` - уровень логирования для всего Kopycat , или для указанного модуля (например, `KOPYCAT_VEOS_LOGGING_CONF=OFF` деактивирует все сообщения, or `KOPYCAT_VEOS_LOGGING_CONF=TimeAPI=OFF,StdlibAPI=FINE` - деактивирует логирования для TimeAPI устанавливает минимальный уровень логирования для StdlibAPI на FINE.

Например:

```
export KOPYCAT_VEOS_LOGGING_CONF=OFF
kopycat-veos-arm usr/bin/readelf -S /usr/bin/readelf
```

Пример запуска:

```
13:52:05 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.115-Regular [JRE v11.0.6]
13:52:05 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
There are 28 section headers, starting at offset 0x862fc:

Section Headers:
  [Nr] Name              Type            Addr     Off    Size   ES Flg Lk Inf Al
  [ 0]                   NULL            00000000 000000 000000 00      0   0  0
  [ 1] .interp           PROGBITS        00000000 000154 000013 00   A  0   0  1
  [ 2] .note.ABI-tag     NOTE            00000000 000168 000020 00   A  0   0  4
  [ 3] .note.gnu.build-i NOTE            00000000 000188 000024 00   A  0   0  4
  [ 4] .gnu.hash         GNU_HASH        00000000 0001ac 00015c 04   A  5   0  4
  [ 5] .dynsym           DYNSYM          00000000 000308 000750 10   A  6   3  4
  [ 6] .dynstr           STRTAB          00000000 000a58 0004e4 00   A  0   0  1
  [ 7] .gnu.version      VERSYM          00000000 000f3c 0000ea 02   A  5   0  2
  [ 8] .gnu.version_r    VERNEED         00000000 001028 000020 00   A  6   1  4
  [ 9] .rel.dyn          REL             00000000 001048 001c28 08   A  5   0  4
  [10] .rel.plt          REL             00000000 002c70 000210 08  AI  5  23  4
  [11] .init             PROGBITS        00000000 002e80 000010 00  AX  0   0  4
  [12] .plt              PROGBITS        00000000 002e90 00032c 04  AX  0   0  4
  [13] .text             PROGBITS        00000000 0031c0 05d65c 00  AX  0   0  8
  [14] .fini             PROGBITS        00000000 06081c 00000c 00  AX  0   0  4
  [15] .rodata           PROGBITS        00000000 060828 023418 00   A  0   0  4
  [16] .ARM.exidx        ARM_EXIDX       00000000 083c40 000008 00  AL 13   0  4
  [17] .eh_frame         PROGBITS        00000000 083c48 000004 00   A  0   0  4
  [18] .init_array       INIT_ARRAY      00000000 0842f4 000004 04  WA  0   0  4
  [19] .fini_array       FINI_ARRAY      00000000 0842f8 000004 04  WA  0   0  4
  [20] .jcr              PROGBITS        00000000 0842fc 000004 00  WA  0   0  4
  [21] .data.rel.ro      PROGBITS        00000000 084300 000c00 00  WA  0   0  4
  [22] .dynamic          DYNAMIC         00000000 084f00 000100 08  WA  6   0  4
  [23] .got              PROGBITS        00000000 085000 0001cc 04  WA  0   0  4
  [24] .data             PROGBITS        00000000 0851d0 001004 00  WA  0   0  8
  [25] .bss              NOBITS          00000000 0861d4 00269c 00  WA  0   0  8
  [26] .ARM.attributes   ARM_ATTRIBUTES  00000000 0861d4 00002a 00      0   0  1
  [27] .shstrtab         STRTAB          00000000 0861fe 0000fe 00      0   0  1
Key to Flags:
  W (write), A (alloc), X (execute), M (merge), S (strings), I (info),
  L (link order), O (extra OS processing required), G (group), T (TLS),
  C (compressed), x (unknown), o (OS specific), E (exclude),
  y (purecode), p (processor specific)  
```



# Сборка и запуск из исходников



## Запуск ядра из исходников с модулем STM32F042

1. Склонировать проект Kopycat:

   ```
   git clone https://github.com/inforion/kopycat.git
   ```
   
2. Импортировать проект Kopycat в Intellij:

   1. Создать новый проект из существующих источников (указать директорию репозитория).

   2. При создании проекта указать `Import project from external model -> Gradle`.

   3. Выбрать `Import project` при запуске IntelliJ и дождаться завершения индексирования проекта.

      Проект имеет следующую структуру:

      * `buildSrc` - graddle плагин для сборки системы;

      *  `kopycat` - содержит ядро эмулятора и менеджер библиотек. Также в `src/main/kotlin/ru/inforion/lab403/kopycat/modules` представлены встроенные модули для памяти (RAM, ROM, NAND, и т.д.), терминалов, и т.д.;

      * `kopycat-modules` - пользовательские модули (устройства):

        * `cores` - процессорные ядра x86, mips, arm и др.;

        * `mcu` - содержит микроконтроллеры на базе этих ядер (т.е. elanSC520, stm32f0xx и др.);

        * `devices` - содержит устройства на базе mcu.

   cores, mcu, devices - в контексте проекта называются *библиотекой* (*library*), а набор библиотек называется *реестром* (*registry*). При запуске Kopycat необходимо указать путь к *реестру* со скомпилированными модулями с помощью параметра `-y`. Разделение на библиотеки используется только для организации модулей.
   
3. Исходный код для примеров модулей размещен в`kopycat-modules`: `kopycat-modules/misc/examples/src/main/kotlin/ru/inforion/lab403/examples/`. Например, здесь лежат `virtarm`, `stm32f042_bytes` и `stm32f042_ihex`.
   
4. Создайте файл Объекта в этой директории (через ПКМ меню IntelliJ, например, `test`). 

5.  Напишите код вида (этот пример также размещен в файлах исходного кода `stm32f042_bytes`)

   ```kotlin
   package ru.inforion.lab403.examples
   
   import ru.inforion.lab403.common.extensions.hex8
   import ru.inforion.lab403.common.extensions.unhexlify
   import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
   import ru.inforion.lab403.kopycat.cores.base.common.Module
   import ru.inforion.lab403.kopycat.gdbstub.GDBServer
   import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042
   import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal
   
   object test {
       @JvmStatic
       fun main(args: Array<String>) {
           // Create simple firmware
           // movs  r0, #3
           // movs  r1, #7
           // adds  r2, r1, r0
           val firmware = "0000000009000000032007210a18".unhexlify()
   
           // Create top-level module. It's necessarily only one top!
           val top = object : Module(null, "top") {
               // Place STM32F042 inside top module
               val mcu = STM32F042(this, "mcu", firmware)
   
               // Place virtual terminal -> will be created using socat
               // You could create virtual terminal by yourself using socat and specify path to /dev/tty...
               // For windows user you should use Com2Com and specify manually COMX from it
               val term1 = UartSerialTerminal(this, "term1", "socat:")
   
               init {
                   // Make actual connection between STM32F042 and Virtual terminal
                   buses.connect(mcu.ports.usart1_m, term1.ports.term_s)
                   buses.connect(mcu.ports.usart1_s, term1.ports.term_m)
   
                   // ARM debugger already in stm
               }
           }
   
           // initialize and reset top module and all inside
           top.initializeAndResetAsTopInstance()
   
           // start GDB server on port 23946
           val gdb = GDBServer(23946, true, binaryProtoEnabled = false)
   
           // connect GDB and device debugger
           gdb.debuggerModule(top.debugger)
   
           // HERE EMULATOR READ TO WORK WITH GDB
           // Below just code to see different API styles
   
           // step CPU core using debugger
           top.debugger.step()
   
           // read CPU register using debugger API
           var r0 = top.debugger.regRead(0)
           var r15 = top.debugger.regRead(15)
           println("using debugger API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")
   
           // read CPU register using core API
           r0 = top.core.reg(0)
           r15 = top.core.reg(15)
           println("using Core/CPU API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")
   
           // read CPU register using internal CPU API
           val arm = top.core.cpu as AARMCPU
           r0 = arm.regs.r0.value
           r15 = arm.regs.pc.value
           println("using internal API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")
   
           // process here will wait until debugger stop
       }
   }
   ```
   
6. Запустить `fun main(args: Array<String>)`. В логе вы увидите следующие сообщения:

   ```
   15:28:04 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term1: /dev/ttys020 and /dev/ttys021
   15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.mcu.cortexm0.arm for top
   15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.mcu.dbg for top
   15:28:05 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
   15:28:05 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term1'
   15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
   15:28:05 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
   15:28:05 INF ...modules.cortexm0.CORTEXM0.reset(CORTEXM0.kt:58): Setup CORTEX-M0 core PC=0x00000009 MSP=0x00000000
   15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
   15:28:05 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.mcu.dbg for GDB_SERVER(port=23946,alive=true)
   using debugger API: r0 = 0x00000003 r15 = 0x0000000B
   using Core/CPU API: r0 = 0x00000003 r15 = 0x0000000A
   using internal API: r0 = 0x00000003 r15 = 0x0000000A
   15:28:05 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [192.168.69.254:23946]
   ```
   *Примечания*: Разные значения регистра r15 являются результатом следующего соглашения: технически ЦПУ ARM надится по адресу `0x00000008` (устанавливается с помощью второго DWORD прошивки), но последние два бита указывают, в каком режиме работает ЦП (ARM, THUMB). Для Kopycat эта информация хранится в специальной внутренней переменной, но для отладчика мы должны выдать сигнал, что процессор находится в режиме THUMB
   

Аналогичный пример также показан VirtARM с Linux в файле `misc/examples/main/kotlin/ru/inforion/lab403/examples/virtarm.kt`.

## Запуск ядра из исходников с самописным модулем (устройством) на ядре ARMv6M

1. Выполните шаги 1-3 из предыдущего раздела.

2. Создайте структуру для кода модуля:

   1. создайте новую библиотеку или выберите одну из существующих в `kopycat-modules`. Например, далее мы используем библиотеку `mcu`.

   2. в библиотеке `mcu` создайте новую папку с именем вашего модуля (например, `testbench`);

   3. внутри папки (`testbench`) создайте новый файл `build.gradle` со следующим содержимым:

      ```json
      plugins {
          id 'ru.inforion.lab403.gradle.kopycat'  // kopycat gradle build plugin 
      }
      
      group 'ru.inforion.lab403.kopycat'  // choose any group you want but follow package rules of Java
      version '0.1'
      
      buildKopycatModule {
          library = "mcu"  // library must be a similar to modules library (in our case mcu)
          require += "cores:arm"  // comma separated modules dependency - in our case only arm core 
      } 
      ```
      
   4. В файл `settings.gradle` в корневой директории проекта добавьте строку вида: `include(":kopycat-modules:mcu:testbench")`

   5. Переимпортируйте проект и IntelliJ создаст структуру папок. Рядом с `testbench` появится пиктограмма с голубым квадратиком. Если структура не была создана, то создайте её самостоятельно, следуя следующему правилу:
   
      src/main/kotlin/<GROUP>/modules/<MODULE_NAME> - пусть имя директории SRC_DIR, where <GROUP> - группа, которую вы указали в build.gradle file, а <MODULE_NAME> - наименование вашего модуля.
   6. Создайте новый класс Kotlin в директории `SRC_DIR` (рекомендуется следовать правилам именования файлов и классов Kotlin, например, назоваите файл `Testbench`).
   7. Вы можете использовать эту структуру для всех последующих модулей. Кроме того, вы можете поместить несколько устройств в один модуль.
   
3. Напишите код вашего устройства `testbench`:

   ```kotlin
   package ru.inforion.lab403.kopycat.modules.testbench
   
   import ru.inforion.lab403.common.extensions.MHz
   import ru.inforion.lab403.kopycat.cores.base.common.Module
   import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
   import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
   import ru.inforion.lab403.kopycat.modules.memory.RAM
   
   // Kopycat library manager looks for classes inherited from Module class
   class Testbench(parent: Module?, name: String) : Module(parent, name) {
   
       // Add ARMv6 core into testbench device
       // First argument is parent module (where instantiated device fold)
       // Second argument is name (aka designator) can be any unique name  
       val arm = ARMv6MCore(this, "arm", frequency = 10.MHz, ipc = 1.0)
   
       // Add modifiable memory region into testbench device (size = 1 MB)
       val ram = RAM(this, "ram", size = 0x10_0000)
   
       // Create internal buses description for testbench device
       // Buses are somelike wires and used to connect different parts of device 
       inner class Buses : ModuleBuses(this) {
           val mem = Bus("mem")
       }
   
       // Assign new buses description to testbench device
       override val buses = Buses()
   
       // Make actual connection between CORE and RAM
       init {
           arm.ports.mem.connect(buses.mem)
           ram.ports.mem.connect(buses.mem, offset = 0x0000_0000)
       }
   }
   ```

Теперь вы можете скомпилировать и запустить эмулятор двумя способами: иснтанциировать `testbench` или использовать менеджер библиотек Kopycat.

### Инстанциирование

Рассмотрим пример с инстанциированием. 

1. Создайте где-нибуть в директории `object` файл `Starter` (например, в директории `SRC_DIR`).

2. Пример кода приведен ниже (стандартное простое приложение на Kotlin):

   ```kotlin
   package ru.inforion.lab403.kopycat.modules.testbench
   
   import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
   
   object Starter {
       @JvmStatic
       fun main(args: Array<String>) {
           // Create our Testbench device
           val top = Testbench(null, "testbench")
   
           // Initialize it as a top device (device that has no parent)
           top.initializeAndResetAsTopInstance()
   
           // Write some instructions into memory
           top.core.write(WORD, 0x0000_0000, 0x2003) // movs  r0, #3
           top.core.write(WORD, 0x0000_0002, 0x2107) // movs  r1, #7
           top.core.write(WORD, 0x0000_0004, 0x180A) // adds  r2, r1, r0
   
           // Setup program counter
           // Note, that we may use top.arm.cpu.pc but there is some caveat here
           // top.arm.cpu.pc just change PC but don't make flags changing (i.e. change core mode)
           // so be aware when change PC. 
           top.arm.cpu.BXWritePC(0x0000_0000)
   
           // Make a step
           top.arm.step()
           assert(top.core.reg(0) == 3L)
   
           // Make another step
           top.arm.step()
           assert(top.core.reg(1) == 7L)
   
           // And one more step
           top.arm.step()
           assert(top.core.reg(2) == 10L)
       }
   }
   ```
   
3. Запустите приложение (нажмите зеленый треугольник рядом с `fun main(args: Array<String>)`). В терминале вы должны увидеть:

   ```
   12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to testbench.arm for testbench
   12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to testbench.dbg for testbench
   12:25:28 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in testbench...
   12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
   12:25:28 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
   12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module testbench is successfully initialized and reset as a top cell!
   
   Process finished with exit code 0
   ```
Так как мы установили функцию `assert` на значения регистров, то вывод выше означает, что программа выполнена успешно.

4. Добавить возможность подключения с помощью дебаггера GDB (IDA Pro или CLion):

   1. добавить следующую строку после инстанциирования `arm` в классе `Testbench`:

      ```kotlin
      val dbg = ARMDebugger(this, "dbg")
      ```

      

   2. добавьте следующие строки в секцию `init` в класс `Testbench`:

      ```
      dbg.ports.breakpoint.connect(buses.mem)
      dbg.ports.reader.connect(buses.mem)
      ```

      

   3. добавьте следующую строку в конец функции `main` объекта `Starter`:

      ```kotlin
      GDBServer(23946, true, binaryProtoEnabled = false).also { it.debuggerModule(top.debugger) }
      ```

      

   4. После этого мы можете подключиться с помощью IDA Pro, CLion (или иного дебагера) к GDB серверу по порту 23946.


### Использование менеджера

Использование менеджера может пригодится для динамической загрузки модулей из библиотеки.

1. Запустите Run конфигурацию как показано на скриншоте ниже. Сначала добавьте конфигурацию `kopycat-arm-build`.

   ![](https://user-images.githubusercontent.com/2856140/104707644-bcf13000-572d-11eb-9f19-f28184d07402.png)
   
2. Затем добавьте `kopycat-testbench-build`. Обратите внимание на *Before Launch* раздел в нижней части экрана.

 ![](https://user-images.githubusercontent.com/2856140/104707659-bfec2080-572d-11eb-8af3-ffd93ba65efd.png)

3. И наконец `kopycat-testbench` (также обратите внимание на *Before Launch* раздел в нижней части экрана).

 ![](https://user-images.githubusercontent.com/2856140/104708043-3ee15900-572e-11eb-926c-cee5b87d1443.png)

4. Запустите конфигурацию `kopycat-testbench` и после успешного запуска вы увидете следующие лог:

   ```
   12:28:09 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.115-Regular [JRE v11.0.6]
   12:28:10 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
   12:28:13 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat/Documents/repos/kopycat-private/temp'
   12:28:13 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
   12:28:13 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): Testbench(null, top)
   12:28:13 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [192.168.76.24:23946]
   12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.arm for top
   12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.dbg for top
   12:28:14 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
   12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
   12:28:14 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
   12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
   12:28:14 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[Testbench] with arm[ARMv6MCore] is ready
   12:28:14 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.dbg for GDB_SERVER(port=23946,alive=true)
   12:28:14 WRN ...at.KopycatStarter.console(KopycatStarter.kt:44): Use -kts option to enable Kotlin console. In the next version Kotlin console will be default.
   12:28:14 CFG ...at.consoles.jep.JepLoader.load(JepLoader.kt:53): Loading Jep using Python command 'python3' to overwrite use '--python' option
   12:28:14 CFG ...oles.jep.PythonShell.version(PythonShell.kt:34): Python Version(major=3, minor=9, micro=0)
   12:28:14 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep jar file: /usr/local/lib/python3.9/site-packages/jep/jep-3.9.1.jar
   12:28:14 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep shared library file: /usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so
   Jep starting successfully!
   12:28:15 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Python console enabled
   Python > 
   ```

   *Примечание*: в данном случае мы не добавляли никаких данных в RAM и не установили PC. Вы можете это сделать внутри класса `Testbench`.
# REST API

Разработчики могут взаимодействовать с Kopycat через REST API. REST API включает три основных API:

* Kopycat (`/kopycat`);
* Registry (`/registry`);
* Console (`/console`) .

Для запуска Kopycat c REST API необходимо прописать его порт в ключе `-r`. Например:

```
kopycat -l mcu -n stm32f042_example -g 23946 -r 3737 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
```

Примеры запросов и ответов для API, а также документация на конечные точки, предоставляет в виде коллекции Postman.


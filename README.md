# snips-demo-smart-house-android

A demo of the snips platform for Android on a smart house theme. The mains screen represents a virtual house composed of multiple rooms and the status of lights, thermostats and windows blinds. Voice can be used to control the different appliances.

## Requirements

Android 5.0+ phone/tablet (arm, arm64, x86 or x86_64 architecture)

## Building and running

```
$ ./gradlew installDebug
$ adb shell am start -n ai.snips.smarthousedemo/.MainActivity
```

Or `MAJ+F10` in Android Studio

## How to use

One the application has finished loading, you can say the wake word (Hey Snips) and ask what you want to do in the virtual house, for example 

> "Hey Snips! Turn the lights on in the kitchen"

> "Hey Snips! Open the blinds in the living room and the bedroom at 70%"

> "Hey Snips! It's too cold in Dani's bedroom"

## Assistant

The Snips assistant in this demo uses the following public apps available on the Snips console

- [Smart Lights](https://console.snips.ai/store/en/skill_G7pVg7Ze8D2)
- [Smart Thermostat](https://console.snips.ai/store/en/skill_Yar6P7P15b26)
- [Smart Windows Devices](https://console.snips.ai/store/en/skill_MpEqxY5nnxb)

Updating the assistant is done by replacing the `src/main/assets/assistant.zip` file with the new assistant. You should also bump the version of the app (ie change at least the `versionPatch` in `build.gradle`) to ensure the new assistant in unzip on older installations.


## App architecture

The architecture used it fairly standard and based on the Android architecture components. There is however no persistance mecanism implemented (ie the virtual house will be reset to a default initial state
across reboots of the app). The file `Rooms.kt` would be the entry point to implement such a system (or a connection to a real home automation system)


## License
### Apache 2.0/MIT

Licensed under either of
 * Apache License, Version 2.0 ([LICENSE-APACHE](LICENSE-APACHE) or
http://www.apache.org/licenses/LICENSE-2.0)
 * MIT license ([LICENSE-MIT](LICENSE-MIT) or
http://opensource.org/licenses/MIT)

     at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally
submitted for inclusion in the work by you, as defined in the Apache-2.0
license, shall be dual licensed as above, without any additional terms
or conditions.




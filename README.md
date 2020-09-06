# appbooster-sdk-android

Mobile framework for Appbooster platform.

## Installation

1. On project level `build.gradle` add `jcenter` repository to all projects section
```
    allprojects {
        // ...
        repositories {
            jcenter()
        // ...
        }
    }
```

2. Add the library to `app` level `build.gradle` dependencies section
```
    dependencies {
        // ...
        implementation "com.appbooster.appboostersdk:appboostersdk:${appbostersdk_version}"
        // ...
    }
```

## Usage


### Initialization:

Kotlin:
```
val sdk = AppboosterSdk.Builder(context) // you can initiate sdk using either application or activity context
            .appId("${YOUR_APP_ID}")
            .sdkToken("${YOUR_SDK_TOKEN}")
            .deviceId("${YOUR_DEVICE_ID}") // optional, UUID generated by default
            .usingShake(false) // true by default for debug mode, turn it off if you are already using shake motion in your app for other purposes
            .defaults(
                    mapOf(
                        "${TEST_1_KEY}" to "${TEST_1_DEFAULT_VALUE}",
                        "${TEST_2_KEY}" to "${TEST_2_DEFAULT_VALUE}"
                )
            )
            .build()
```

Java:
```
Map<String, String> defaults = new HashMap<>();
defaults.put("android_1", "");
defaults.put("android_2", "");
        
AppboosterSdk sdk = new AppboosterSdk.Builder(this)
    .appId(YOUR_APP_ID)
    .sdkToken(YOUR_SDK_TOKEN)
    .deviceId(YOUR_DEVICE_ID) // optional, UUID generated by default
    .usingShake(true) // true by default for debug mode, turn it off if you are already using shake motion in your app for other purposes
    .defaults(defaults)
    .build();
```

### How to fetch known tests values that associated with your device?

Kotlin:
```
sdk.fetch(onSuccessListener = object: AppboosterSdk.OnSuccessListener{
            override fun onSuccess() {
            }
        },
        onErrorListener = object: AppboosterSdk.OnErrorListener{
            override fun onError(throwable: Throwable) {
            }
        })
```

Java:
```
sdk.fetch(onSuccessListener = object: AppboosterSdk.OnSuccessListener{
            override fun onSuccess() {
            }
        },
        onErrorListener = object: AppboosterSdk.OnErrorListener{
            override fun onError(throwable: Throwable) {
            }
        });
```

### How to get the value for a specific test?

Kotlin:
```
val value = sdk["${TEST_KEY}"]
```

Java:
```
String value = sdk.getValue("${TEST_KEY}");
```

In case of problems with no internet connection or another, the values obtained in the previous session will be used, or if they are missing, the default values specified during initialization will be used.

### How to get user tests for analytics?

Kotlin:
```
val experiments = sdk.experiments
```

Java:
```
List<Experiment> experiments = sdk.getExperiments();
```

### How to debug?

Kotlin:
```
sdk.showDebugLogs = true // false by default, to print all debugging info in the console
val duration = sdk.lastOperationDurationMillis // the duration of the last operation in milliseconds
```

Java:
```
sdk.setShowDebugLogs(true); // false by default, to print all debugging info in the console
long duration = sdk.getLastOperationDurationMillis(); 
```

In debug mode you can see all actual tests and check how the user will see each option of the test.
To show the debug activity you just need to turn it on in your personal cabinet and call

Kotlin:
```
AppboosterDebugActivity.launch(context: Context)
```

Java:
```
AppboosterDebugActivity.launch(Context context); // you can use either application or activity context
```

By default debug activity will be shown by performing shake motion on your device.


==================================================

You can see the example of usage in `ExampleAppKotlin` and `ExampleAppJava` modules in this project.
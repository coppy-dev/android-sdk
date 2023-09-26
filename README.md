# Coppy Android SDK

Coppy SDK for Android consists of two major parts:

1. Gradle plugin, which is responsible for generating Kotlin classes that will be used by the app in the runtime. These classes also provide IDE autocompletion and better dev experience.
2. The runtime module, which is responsible for downloading new versions of content and updating the copy in the app.

## Prerequisities

1. **Android Compose UI toolkit**. At the moment, Coppy only works with projects that are built with Android Compose UI toolkit. Compose UI provide us with all necessary abstractions that allow us to effeciently update app copy in the runtime.

2. **Coppy content key**. Content key tells Coppy plugin and runtime SDK how to get your specific content. To get a content key, go to your [Coppy profile page](https://app.coppy.app/profile) and select sepcific team, which content you want to use in the Android app. The content key will righ tbelow the team name.

## Getting started

### Add plugin

To get started with Coppy SDk, you need to add a Coppy plugin first. Add it to plugins section in your app `build.gradle` file. Then, add the content key to the Coppy plugin config:

```diff
plugins {
    id("com.android.application")
+    id("app.coppy") version("1.0.0")
}

+coppy {
+    contentKey = "<YOUR_CONTENT_KEY>"
+}
```

After that you need to run gradle sync and build the project, so Coppy plugin can generate runtime content classes

### Using copy at runtime

To use coppy in your app you need to first initializ it in your main activity:

```diff
+import app.coppy.Coppy
+import app.coppy.generatedCoppy.CoppyContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

+       Coppy.initialize(applicationContext, CoppyContent::class.java)

        setContent {
            App()
        }
    }
}
```

After that you can use coppy content in your component:

```diff
+import app.coppy.Coppy
+import app.coppy.generatedCoppy.CoppyContent

@Preview(showBackground = true)
@Composable
fun IntroScreen (
    onClick: () -> Unit = {}
) {
+    val intro = Coppy.useContent(CoppyContent::class.java).collectAsState().value.features.intro
    Column(
        Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(1f), verticalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
-            Text(text = "Welcome Back!", fontSize = 30.sp)
+            Text(text = intro.title, fontSize = 30.sp)
-            Text(text = "Happy to see you again! Here are some things you've might missed", fontSize = 16.sp)
+            Text(text = intro.body, fontSize = 16.sp)

        }
        Column(Modifier.padding(16.dp, 12.dp)) {
            Button(onClick = onClick, modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)) {
-                Text(text = "Get Started")
+                Text(text = intro.cta)
            }
        }
    }
}
```

## Configuration

At the moment there are few things you can configure in how Coppy SDK works:

1. **Update interval (`updateInterval`)** — interval in minutes for how often Coppy SDK should check for the new content version. By default it is 30 minutes.t

2. **Update type (`updateType`)** — defines how Coppy will update app copy in the runtime. Note, that it still will check for copy updates withing specified interval (`updateInterval`). But, depending on update type setting, it might not apply those changes immediately. Instead it will persist them locally and use for the next copy update.
   - `default` — by default, Coppy will check only update copy when app is hard-reloaded (i.e user closes the app, and opens it again).
   - `background` — Coppy will update the copy when app is backgrounded. Note, that because Compose UI does not run in background, the actual copy update will happen when the app is coming back from background into foreground.
   - `foreground` — Coppy will will updat ethe copy in the app as soon as it gets the new version of content from the server.

```diff
plugins {
    id("com.android.application")
    id("app.coppy")
}

coppy {
    contentKey = "<YOUR_CONTENT_KEY>"
+    updateInterval = 15
+    updateType = "foreground"
}
```

## Ejecting

If you no longer want to use Coppy and pay for it, you still can leave its SDK in your project. You don't need to make a huge app refactoring and replace all cases of using Coppy with hard-coded copy. Your copy will stay in the app for as long as you need to.

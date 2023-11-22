# Coppy Android SDK

Coppy SDK for Android consists of two major parts:

1. Gradle plugin, which is responsible for generating Kotlin classes that will be used by the app in the runtime. These classes also provide IDE autocompletion and better dev experience.
2. The runtime module, which is responsible for downloading new versions of content and updating the copy in the app.

## Prerequisities

1. **Android Compose UI toolkit**. At the moment, Coppy only works with projects that are built with the Android Compose UI toolkit. Compose UI provides us with all necessary abstractions that allow us to efficiently update app copy in the runtime.

2. **Coppy content key**. The content key tells the Coppy plugin and runtime SDK how to get your specific content. To get a content key, go to your [Coppy profile page](https://app.coppy.app/profile) and select a specific team, which content you want to use in the Android app. The content key will be right below the team name.
<picture width="1280" role="presentation">
    <source media="(prefers-color-scheme: dark)" srcset="https://coppy.app/images/docs/content-key-dark.png" />
    <source media="(prefers-color-scheme: light)" srcset="https://coppy.app/images/docs/content-key.png" />
    <img src="https://coppy.app/images/docs/content-key.png" />
</picture>

## Getting started

### Add plugin

To get started with Coppy SDK, you need to add a Coppy plugin first. Add it to the plugins section in your app `build.gradle` file. Then, add the content key to the Coppy plugin config:

```diff
plugins {
    id("com.android.application")
+    id("app.coppy") version("1.0.0")
}

+coppy {
+    contentKey = "<YOUR_CONTENT_KEY>"
+}
```

After that, you need to run gradle sync and build the project so the Coppy plugin can generate runtime content classes.

### Using copy at runtime

To use coppy in your app, you need to first initialize it in your main activity:

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

Then, you can use coppy content in your component:

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

At the moment, there are a few things you can configure in how Coppy SDK works:

1. **Update interval (`updateInterval`)** — interval in minutes for how often Coppy SDK should check for the new content version. By default, it is 30 minutes.t

2. **Update type (`updateType`)** — defines how Coppy will update the app copy in the runtime. Note that it will still check for copy updates within specified intervals (`updateInterval`). But, depending on the update type setting, it might not apply those changes immediately. Instead, it will store them locally and will use them for the next copy update. By default (if option is not set), Coppy will check only update copy when app is hard-reloaded (i.e user closes the app, and opens it again).
   - `background` — Coppy will update the copy when the app is backgrounded. Note that because Compose UI does not run in the background, the actual copy update will happen when the app comes back from the background into the foreground.
   - `foreground` — Coppy will update the copy in the app as soon as it gets the new version of content from the server.

```diff
plugins {
    id("com.android.application")
    id("app.coppy") version("1.0.0")
}

coppy {
    contentKey = "<YOUR_CONTENT_KEY>"
+    updateInterval = 15
+    updateType = "foreground"
}
```

## Ejecting

If you no longer want to use Coppy and pay for it, you can still leave its SDK in your project. You don't need to make a huge app refactoring and replace all cases of using Coppy with the hard-coded copy. Your copy will stay in the app for as long as needed.

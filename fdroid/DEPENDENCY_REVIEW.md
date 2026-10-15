# Release dependency review

Reviewed for version 1.0.0 using `:app:dependencies --configuration
releaseRuntimeClasspath`. The 120 entries below include BOMs and constraints;
this is a resolved dependency inventory, not a count of bundled libraries.

All listed artifacts declare Apache License 2.0 in their published Maven POM,
or in the parent POM for Guava ListenableFuture. Upstream licenses remain in
force independently of this application's MIT license. Published POM declarations
are evidence of licensing, not an independent audit of every upstream source file.

The unsigned release APK also passed `fdroidserver 2.4.5` binary scanning for
known non-free classes and extra signing blocks. This is a local preflight;
A source-only archive also passed the local fdroidserver source scanner with
zero problems. F-Droid's isolated build and inclusion review remain required.

## Launcher artwork

The Android Studio robot template and unused legacy robot bitmaps have been
replaced with original SMS bubble/arrow vector artwork under the project MIT
license. minSdk 26 supports the adaptive launcher icon used by this app.
Final visual approval remains pending.

## Resolved inventory

| Maven coordinate | Declared license and source |
| --- | --- |
| `androidx.activity:activity:1.13.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/activity/activity/1.13.0/activity-1.13.0.pom) |
| `androidx.activity:activity-compose:1.13.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/activity/activity-compose/1.13.0/activity-compose-1.13.0.pom) |
| `androidx.activity:activity-ktx:1.13.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/activity/activity-ktx/1.13.0/activity-ktx-1.13.0.pom) |
| `androidx.annotation:annotation:1.9.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/annotation/annotation/1.9.1/annotation-1.9.1.pom) |
| `androidx.annotation:annotation-experimental:1.4.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/annotation/annotation-experimental/1.4.1/annotation-experimental-1.4.1.pom) |
| `androidx.annotation:annotation-jvm:1.9.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/annotation/annotation-jvm/1.9.1/annotation-jvm-1.9.1.pom) |
| `androidx.appcompat:appcompat:1.7.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/appcompat/appcompat/1.7.1/appcompat-1.7.1.pom) |
| `androidx.appcompat:appcompat-resources:1.7.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/appcompat/appcompat-resources/1.7.1/appcompat-resources-1.7.1.pom) |
| `androidx.arch.core:core-common:2.2.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/arch/core/core-common/2.2.0/core-common-2.2.0.pom) |
| `androidx.arch.core:core-runtime:2.2.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/arch/core/core-runtime/2.2.0/core-runtime-2.2.0.pom) |
| `androidx.autofill:autofill:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/autofill/autofill/1.0.0/autofill-1.0.0.pom) |
| `androidx.collection:collection:1.5.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/collection/collection/1.5.0/collection-1.5.0.pom) |
| `androidx.collection:collection-jvm:1.5.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/collection/collection-jvm/1.5.0/collection-jvm-1.5.0.pom) |
| `androidx.collection:collection-ktx:1.5.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/collection/collection-ktx/1.5.0/collection-ktx-1.5.0.pom) |
| `androidx.compose:compose-bom:2026.03.01` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/compose-bom/2026.03.01/compose-bom-2026.03.01.pom) |
| `androidx.compose.animation:animation:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/animation/animation/1.10.6/animation-1.10.6.pom) |
| `androidx.compose.animation:animation-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/animation/animation-android/1.10.6/animation-android-1.10.6.pom) |
| `androidx.compose.animation:animation-core:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/animation/animation-core/1.10.6/animation-core-1.10.6.pom) |
| `androidx.compose.animation:animation-core-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/animation/animation-core-android/1.10.6/animation-core-android-1.10.6.pom) |
| `androidx.compose.foundation:foundation:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/foundation/foundation/1.10.6/foundation-1.10.6.pom) |
| `androidx.compose.foundation:foundation-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/foundation/foundation-android/1.10.6/foundation-android-1.10.6.pom) |
| `androidx.compose.foundation:foundation-layout:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/foundation/foundation-layout/1.10.6/foundation-layout-1.10.6.pom) |
| `androidx.compose.foundation:foundation-layout-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/foundation/foundation-layout-android/1.10.6/foundation-layout-android-1.10.6.pom) |
| `androidx.compose.material:material-ripple:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/material/material-ripple/1.10.6/material-ripple-1.10.6.pom) |
| `androidx.compose.material:material-ripple-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/material/material-ripple-android/1.10.6/material-ripple-android-1.10.6.pom) |
| `androidx.compose.material3:material3:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3/1.4.0/material3-1.4.0.pom) |
| `androidx.compose.material3:material3-android:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3-android/1.4.0/material3-android-1.4.0.pom) |
| `androidx.compose.runtime:runtime:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime/1.10.6/runtime-1.10.6.pom) |
| `androidx.compose.runtime:runtime-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime-android/1.10.6/runtime-android-1.10.6.pom) |
| `androidx.compose.runtime:runtime-annotation:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime-annotation/1.10.6/runtime-annotation-1.10.6.pom) |
| `androidx.compose.runtime:runtime-annotation-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime-annotation-android/1.10.6/runtime-annotation-android-1.10.6.pom) |
| `androidx.compose.runtime:runtime-retain:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime-retain/1.10.6/runtime-retain-1.10.6.pom) |
| `androidx.compose.runtime:runtime-retain-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime-retain-android/1.10.6/runtime-retain-android-1.10.6.pom) |
| `androidx.compose.runtime:runtime-saveable:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime-saveable/1.10.6/runtime-saveable-1.10.6.pom) |
| `androidx.compose.runtime:runtime-saveable-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/runtime/runtime-saveable-android/1.10.6/runtime-saveable-android-1.10.6.pom) |
| `androidx.compose.ui:ui:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui/1.10.6/ui-1.10.6.pom) |
| `androidx.compose.ui:ui-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-android/1.10.6/ui-android-1.10.6.pom) |
| `androidx.compose.ui:ui-geometry:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-geometry/1.10.6/ui-geometry-1.10.6.pom) |
| `androidx.compose.ui:ui-geometry-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-geometry-android/1.10.6/ui-geometry-android-1.10.6.pom) |
| `androidx.compose.ui:ui-graphics:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-graphics/1.10.6/ui-graphics-1.10.6.pom) |
| `androidx.compose.ui:ui-graphics-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-graphics-android/1.10.6/ui-graphics-android-1.10.6.pom) |
| `androidx.compose.ui:ui-text:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-text/1.10.6/ui-text-1.10.6.pom) |
| `androidx.compose.ui:ui-text-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-text-android/1.10.6/ui-text-android-1.10.6.pom) |
| `androidx.compose.ui:ui-tooling-preview:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-tooling-preview/1.10.6/ui-tooling-preview-1.10.6.pom) |
| `androidx.compose.ui:ui-tooling-preview-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-tooling-preview-android/1.10.6/ui-tooling-preview-android-1.10.6.pom) |
| `androidx.compose.ui:ui-unit:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-unit/1.10.6/ui-unit-1.10.6.pom) |
| `androidx.compose.ui:ui-unit-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-unit-android/1.10.6/ui-unit-android-1.10.6.pom) |
| `androidx.compose.ui:ui-util:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-util/1.10.6/ui-util-1.10.6.pom) |
| `androidx.compose.ui:ui-util-android:1.10.6` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/compose/ui/ui-util-android/1.10.6/ui-util-android-1.10.6.pom) |
| `androidx.concurrent:concurrent-futures:1.1.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/concurrent/concurrent-futures/1.1.0/concurrent-futures-1.1.0.pom) |
| `androidx.core:core:1.18.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/core/core/1.18.0/core-1.18.0.pom) |
| `androidx.core:core-ktx:1.18.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/core/core-ktx/1.18.0/core-ktx-1.18.0.pom) |
| `androidx.core:core-viewtree:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/core/core-viewtree/1.0.0/core-viewtree-1.0.0.pom) |
| `androidx.cursoradapter:cursoradapter:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/cursoradapter/cursoradapter/1.0.0/cursoradapter-1.0.0.pom) |
| `androidx.customview:customview:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/customview/customview/1.0.0/customview-1.0.0.pom) |
| `androidx.customview:customview-poolingcontainer:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/customview/customview-poolingcontainer/1.0.0/customview-poolingcontainer-1.0.0.pom) |
| `androidx.documentfile:documentfile:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/documentfile/documentfile/1.0.0/documentfile-1.0.0.pom) |
| `androidx.drawerlayout:drawerlayout:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/drawerlayout/drawerlayout/1.0.0/drawerlayout-1.0.0.pom) |
| `androidx.dynamicanimation:dynamicanimation:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/dynamicanimation/dynamicanimation/1.0.0/dynamicanimation-1.0.0.pom) |
| `androidx.emoji2:emoji2:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/emoji2/emoji2/1.4.0/emoji2-1.4.0.pom) |
| `androidx.emoji2:emoji2-views-helper:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/emoji2/emoji2-views-helper/1.4.0/emoji2-views-helper-1.4.0.pom) |
| `androidx.fragment:fragment:1.5.4` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/fragment/fragment/1.5.4/fragment-1.5.4.pom) |
| `androidx.graphics:graphics-path:1.0.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/graphics/graphics-path/1.0.1/graphics-path-1.0.1.pom) |
| `androidx.interpolator:interpolator:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/interpolator/interpolator/1.0.0/interpolator-1.0.0.pom) |
| `androidx.legacy:legacy-support-core-utils:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/legacy/legacy-support-core-utils/1.0.0/legacy-support-core-utils-1.0.0.pom) |
| `androidx.lifecycle:lifecycle-common:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-common/2.10.0/lifecycle-common-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-common-java8:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-common-java8/2.10.0/lifecycle-common-java8-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-common-jvm:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-common-jvm/2.10.0/lifecycle-common-jvm-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-livedata:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-livedata/2.10.0/lifecycle-livedata-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-livedata-core:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-livedata-core/2.10.0/lifecycle-livedata-core-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-livedata-core-ktx:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-livedata-core-ktx/2.10.0/lifecycle-livedata-core-ktx-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-process:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-process/2.10.0/lifecycle-process-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-runtime:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-runtime/2.10.0/lifecycle-runtime-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-runtime-android:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-runtime-android/2.10.0/lifecycle-runtime-android-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-runtime-compose:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-runtime-compose/2.10.0/lifecycle-runtime-compose-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-runtime-compose-android:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-runtime-compose-android/2.10.0/lifecycle-runtime-compose-android-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-runtime-ktx:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-runtime-ktx/2.10.0/lifecycle-runtime-ktx-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-runtime-ktx-android:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-runtime-ktx-android/2.10.0/lifecycle-runtime-ktx-android-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-viewmodel:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel/2.10.0/lifecycle-viewmodel-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-viewmodel-android:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-android/2.10.0/lifecycle-viewmodel-android-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-compose/2.10.0/lifecycle-viewmodel-compose-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-viewmodel-compose-android:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-compose-android/2.10.0/lifecycle-viewmodel-compose-android-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-ktx/2.10.0/lifecycle-viewmodel-ktx-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-savedstate/2.10.0/lifecycle-viewmodel-savedstate-2.10.0.pom) |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate-android:2.10.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-savedstate-android/2.10.0/lifecycle-viewmodel-savedstate-android-2.10.0.pom) |
| `androidx.loader:loader:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/loader/loader/1.0.0/loader-1.0.0.pom) |
| `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/localbroadcastmanager/localbroadcastmanager/1.0.0/localbroadcastmanager-1.0.0.pom) |
| `androidx.navigationevent:navigationevent:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/navigationevent/navigationevent/1.0.0/navigationevent-1.0.0.pom) |
| `androidx.navigationevent:navigationevent-android:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/navigationevent/navigationevent-android/1.0.0/navigationevent-android-1.0.0.pom) |
| `androidx.navigationevent:navigationevent-compose:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/navigationevent/navigationevent-compose/1.0.0/navigationevent-compose-1.0.0.pom) |
| `androidx.navigationevent:navigationevent-compose-android:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/navigationevent/navigationevent-compose-android/1.0.0/navigationevent-compose-android-1.0.0.pom) |
| `androidx.print:print:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/print/print/1.0.0/print-1.0.0.pom) |
| `androidx.profileinstaller:profileinstaller:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/profileinstaller/profileinstaller/1.4.0/profileinstaller-1.4.0.pom) |
| `androidx.resourceinspection:resourceinspection-annotation:1.0.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/resourceinspection/resourceinspection-annotation/1.0.1/resourceinspection-annotation-1.0.1.pom) |
| `androidx.savedstate:savedstate:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/savedstate/savedstate/1.4.0/savedstate-1.4.0.pom) |
| `androidx.savedstate:savedstate-android:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/savedstate/savedstate-android/1.4.0/savedstate-android-1.4.0.pom) |
| `androidx.savedstate:savedstate-compose:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/savedstate/savedstate-compose/1.4.0/savedstate-compose-1.4.0.pom) |
| `androidx.savedstate:savedstate-compose-android:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/savedstate/savedstate-compose-android/1.4.0/savedstate-compose-android-1.4.0.pom) |
| `androidx.savedstate:savedstate-ktx:1.4.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/savedstate/savedstate-ktx/1.4.0/savedstate-ktx-1.4.0.pom) |
| `androidx.startup:startup-runtime:1.1.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/startup/startup-runtime/1.1.1/startup-runtime-1.1.1.pom) |
| `androidx.tracing:tracing:1.2.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/tracing/tracing/1.2.0/tracing-1.2.0.pom) |
| `androidx.transition:transition:1.6.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/transition/transition/1.6.0/transition-1.6.0.pom) |
| `androidx.vectordrawable:vectordrawable:1.1.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/vectordrawable/vectordrawable/1.1.0/vectordrawable-1.1.0.pom) |
| `androidx.vectordrawable:vectordrawable-animated:1.1.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/vectordrawable/vectordrawable-animated/1.1.0/vectordrawable-animated-1.1.0.pom) |
| `androidx.versionedparcelable:versionedparcelable:1.1.1` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/versionedparcelable/versionedparcelable/1.1.1/versionedparcelable-1.1.1.pom) |
| `androidx.viewpager:viewpager:1.0.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/viewpager/viewpager/1.0.0/viewpager-1.0.0.pom) |
| `androidx.window:window:1.5.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/window/window/1.5.0/window-1.5.0.pom) |
| `androidx.window:window-core:1.5.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/window/window-core/1.5.0/window-core-1.5.0.pom) |
| `androidx.window:window-core-android:1.5.0` | [Apache-2.0](https://dl.google.com/dl/android/maven2/androidx/window/window-core-android/1.5.0/window-core-android-1.5.0.pom) |
| `com.google.guava:listenablefuture:1.0` | [Apache-2.0](https://repo.maven.apache.org/maven2/com/google/guava/guava-parent/26.0-android/guava-parent-26.0-android.pom) |
| `org.jetbrains:annotations:23.0.0` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/annotations/23.0.0/annotations-23.0.0.pom) |
| `org.jetbrains.kotlin:kotlin-stdlib:2.3.20` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.3.20/kotlin-stdlib-2.3.20.pom) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-android/1.9.0/kotlinx-coroutines-android-1.9.0.pom) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.9.0` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-bom/1.9.0/kotlinx-coroutines-bom-1.9.0.pom) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core/1.9.0/kotlinx-coroutines-core-1.9.0.pom) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.9.0/kotlinx-coroutines-core-jvm-1.9.0.pom) |
| `org.jetbrains.kotlinx:kotlinx-serialization-bom:1.7.3` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-bom/1.7.3/kotlinx-serialization-bom-1.7.3.pom) |
| `org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-core/1.7.3/kotlinx-serialization-core-1.7.3.pom) |
| `org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.7.3` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-core-jvm/1.7.3/kotlinx-serialization-core-jvm-1.7.3.pom) |
| `org.jspecify:jspecify:1.0.0` | [Apache-2.0](https://repo.maven.apache.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.pom) |

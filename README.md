# [DragGrid](https://github.com/edhlily/DragGrid)

## Demo【演示效果】

![](https://github.com/edhlily/DragGrid/raw/main/screen.gif)

##  Use And Config【如何使用】

```xml
<com.edhlily.draggrid.DragGridLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/colorBg"
    android:padding="20dp"
    app:allowItemOutside="false"
    app:colCount="4"
    app:colSpacing="20dp"
    app:dragOrientation="all"
    app:rowCount="4"
    app:rowSpacing="20dp"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/v1"
        style="@style/ITEM"
        android:onClick="onItemClick"
        android:text="1"
        app:layout_column="0"
        app:layout_row="0" />
    ...//add other child item here or add child programmatically [添加子Item或者在程序中动态添加]
</com.edhlily.draggrid.DragGridLayout>
```

```xml
<attr name="dragOrientation">
        <!-- Item can drag all direction [可以任意方向拖动]-->
        <enum name="all" value="0" />
        <!-- Item can only drag in vertical [只可以纵向拖动]-->
        <enum name="vertical" value="1" />
        <!-- Item can only drag in horizontal [只可以横向拖动]-->
        <enum name="horizontal" value="2" />
    </attr>

    <declare-styleable name="DragGridLayout">
        <!-- grid row [行]-->
        <attr name="rowCount" format="integer" min="1" />
        <!-- grid column [列]-->
        <attr name="colCount" format="integer" min="1" />
        <!-- grid row spacing [行间距]-->
        <attr name="rowSpacing" format="dimension" />
        <!-- grid column spacing [列间距]-->
        <attr name="colSpacing" format="dimension" />
        <!-- Item magnification default 1.1 [被拖动的放大倍数 默认1.1]-->
        <attr name="dragScale" format="float" />
        <!-- Set if item draggable default true [Item是否可以被拖动 默认true]-->
        <attr name="draggable" format="float" />
        <!-- Item drag orientation default all [可以拖动的方向,默认all,可以所有方向拖动] -->
        <attr name="dragOrientation" />
        <!-- Set if item can be dragged outside of layout default false [是否可以拖动超出边界，默认false] -->
        <attr name="allowItemOutside" format="boolean" />
    </declare-styleable>

    <declare-styleable name="DragGridLayout_Layout">
        <!-- Item row [所在行]-->
        <attr name="layout_row" format="integer" min="0" />
        <!-- Item row span default 1[所占行数 默认1]-->
        <attr name="layout_rowSpan" format="integer" min="1" />
        <!-- Item column [所在列]-->
        <attr name="layout_column" format="integer" min="0" />
        <!-- Item column span default 1 [所占列数 默认1]-->
        <attr name="layout_columnSpan" format="integer" min="1" />
    </declare-styleable>
```


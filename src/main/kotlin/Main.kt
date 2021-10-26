import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.util.*


class Main : Application() {
    private val layout = BorderPane()
    private val homeDir = File("${System.getProperty("user.dir")}/test/")
    private var currDir: File = homeDir
    val listView = ListView<String>()
    val isHidden = true;
    override fun start(primaryStage: Stage) {

        //region Top Menu
        val menuBar = MenuBar()
        val fileMenu = Menu("File")
        val fileNew = MenuItem("New")
        val viewMenu = Menu("View")
        val dirPrev = MenuItem("Prev")
        val dirNext = MenuItem("Next")
        val dirHome = MenuItem("Home")
        val actionsMenu = Menu("Actions")
        val fileRename = MenuItem("Rename")
        val fileMove = MenuItem("Move")
        val fileDelete = MenuItem("Delete")
        val optionsMenu = Menu("Options")
        val hiddenToggle = RadioMenuItem("Show Hidden Files")

        menuBar.menus.addAll(fileMenu, viewMenu, actionsMenu, optionsMenu)
        fileMenu.items.add(fileNew)
        viewMenu.items.addAll(dirPrev, dirNext, dirHome)
        actionsMenu.items.addAll(fileRename, fileMove, fileDelete)
        optionsMenu.items.add(hiddenToggle)
        //endregion

        //region Navigation Bar
        val navBar = ToolBar()
        val homeButton = Button("Home")
        homeButton.graphic = ImageView(Image("homeIcon.png",15.00, 15.00, true, true))
        val prevButton = Button("Prev")
        prevButton.graphic = ImageView(Image("prevIcon.png",15.00, 15.00, true, true))
        val nextButton = Button("Next")
        nextButton.graphic = ImageView(Image("nextIcon.png",15.00, 15.00, true, true))
        nextButton.contentDisplay = ContentDisplay.RIGHT
        val deleteButton = Button("Delete")
        deleteButton.graphic = ImageView(Image("deleteIcon.png", 15.00, 15.00, true, true))
        val renameButton = Button("Rename")
        renameButton.graphic = ImageView(Image("renameIcon.png", 15.00, 15.00, true, true))
        val moveButton = Button("Move")
        moveButton.graphic = ImageView(Image("moveIcon.png", 15.00, 15.00, true, true))

        navBar.items.addAll(homeButton, prevButton, nextButton, deleteButton, renameButton, moveButton)
        //endregion

        renameButton.setOnAction {
            val input = renamePrompt("Rename File", "New File Name")
            if(input == ""){
                val alert = Alert(Alert.AlertType.ERROR)
                alert.headerText = null
                alert.graphic = null
                alert.contentText = "Invalid File Name - Input a new file name."
                alert.show()
            } else if (input != null) {
                resizePreview(listView.selectionModel.selectedItem)
            }
        }

        moveButton.setOnAction {
            try{
                movePrompt("Move File or Directory", "New Directory Name")
            } catch (e: IOException){
                val alert = Alert(Alert.AlertType.ERROR)
                alert.headerText = null
                alert.graphic = null
                alert.contentText = "Failed to move file or directory"
                alert.show()
            }
            resizePreview(listView.selectionModel.selectedItem)
        }

        //region Top Container
        val topContainer = VBox()
        topContainer.children.add(menuBar)
        topContainer.children.add(navBar)
        layout.top = topContainer
        //endregion

        //region List of Files
        val files: List<File> = currDir.listFiles().toList()
        for (item in files) {
            if (isHidden && item.name.startsWith(".")) {
            } else {
                listView.items.add(item.name)
            }
        }
        listView.selectionModel.selectionMode = SelectionMode.SINGLE
        listView.selectionModel.select(0)
        layout.left = listView
        resizePreview(listView.selectionModel.selectedItem)

        //region descend/ascend/home actions
        listView.setOnKeyPressed { key: KeyEvent ->
            if (key.code.equals(KeyCode.ENTER)) {
                descend()
            } else if (key.code.equals(KeyCode.BACK_SPACE) || key.code.equals(KeyCode.DELETE)) {
                ascend()
            } else if (key.code.equals(KeyCode.DOWN) || key.code.equals(KeyCode.UP)) {
                resizePreview(listView.selectionModel.selectedItem)
            }
        }

        nextButton.setOnAction {
            descend()
        }

        prevButton.setOnAction {
            ascend()
        }

        homeButton.setOnAction {
            home()
        }

        deleteButton.setOnAction {
            val input = deletePrompt("Delete File")
            if(input == ""){
                val file = File(currDir.path + "\\" + listView.selectionModel.selectedItem)
                println("hello")
                if (file.exists()) {
                    println("here")
                    file.delete()
                }
            } else if (input != null) {
                reloadList("")
                resizePreview(listView.selectionModel.selectedItem)
            }
        }

        listView.setOnMouseClicked{
                mouseEvent: MouseEvent ->
            if (mouseEvent.clickCount === 2) {
                descend()
            } else {
                resizePreview(listView.selectionModel.selectedItem)
            }
        }

        dirNext.setOnAction {
            descend()
        }

        dirPrev.setOnAction {
            ascend()
        }

        dirHome.setOnAction {
            home()
        }

        fileRename.setOnAction {
            val input = renamePrompt("Rename File", "New File Name")
            if(input == ""){
                val alert = Alert(Alert.AlertType.ERROR)
                alert.headerText = null
                alert.graphic = null
                alert.contentText = "Invalid File Name - Input a new file name."
                alert.show()
            } else if (input != null) {
                resizePreview(listView.selectionModel.selectedItem)
            }
        }

        fileMove.setOnAction {
            try{
                movePrompt("Move File or Directory", "New Directory Name")
            } catch (e: IOException){
                val alert = Alert(Alert.AlertType.ERROR)
                alert.headerText = null
                alert.graphic = null
                alert.contentText = "Failed to move file or directory"
                alert.show()
            }
            resizePreview(listView.selectionModel.selectedItem)
        }

        fileDelete.setOnAction {
            val input = deletePrompt("Delete File")
            if(input == ""){
                val file = File(currDir.path + "\\" + listView.selectionModel.selectedItem)
                if (file.exists()) {
                    file.delete()
                }
            } else if (input != null) {
                reloadList("")
                resizePreview(listView.selectionModel.selectedItem)
            }
        }

        hiddenToggle.setOnAction {
            toggleHidden()
        }

        //endregion

        //endregion

        //region Remove Focus on Menus
        homeButton.focusTraversableProperty().set(false)
        prevButton.focusTraversableProperty().set(false)
        nextButton.focusTraversableProperty().set(false)
        deleteButton.focusTraversableProperty().set(false)
        renameButton.focusTraversableProperty().set(false)
        moveButton.focusTraversableProperty().set(false)
        topContainer.focusTraversableProperty().set(false)
        listView.focusTraversableProperty().set(true)
        //endregion

        //region Scene
        val scene = Scene(layout, 800.0, 500.0)
        listView.requestFocus()
        primaryStage.title = "File Browser"
        primaryStage.isResizable = false
        primaryStage.scene = scene

        primaryStage.show()
        //endregion
    }

    fun reloadList(fileName: String){
        val directory = File(currDir.path)
        if(directory.isDirectory){
            val files = directory.listFiles().toList()
            listView.items.clear()
            for (file in files){
                listView.items.add(file.name)
            }
            if(listView.items.size > 0){
                listView.selectionModel.selectionMode = SelectionMode.SINGLE
                if(fileName != ""){
                    listView.selectionModel.select(fileName)
                } else {
                    listView.selectionModel.select(0)
                }
            }
            currDir = directory
        }
    }

    fun deletePrompt(title: String): String? {
        val popup = Dialog<String>()
        popup.title = title
        popup.headerText = null
        popup.graphic = null
        popup.contentText = "Do you want to delete this file?"
        val type = ButtonType("Ok", ButtonData.OK_DONE)
        popup.dialogPane.buttonTypes.add(type);
        val result = popup.showAndWait()

        if(result.isPresent()) {
            return ""
        }
        return null
    }

    // Input of move requires the absolute path of the directory
    fun movePrompt(title: String, prompt: String) {
        val popup = TextInputDialog()
        popup.title = title
        popup.headerText = null
        popup.graphic = null
        popup.contentText = prompt
        val result = popup.showAndWait()

        val oldFile = File(currDir, listView.selectionModel.selectedItem)

        if(result.isPresent()) {
            val newDir = popup.getEditor().getText()
            val newFile = File(newDir, listView.selectionModel.selectedItem)
            Files.move(oldFile.toPath(), newFile.toPath())
            reloadList("")
        }
    }

    fun renamePrompt(title: String, prompt: String): String? {
        val popup = TextInputDialog()
        popup.title = title
        popup.headerText = null
        popup.graphic = null
        popup.contentText = prompt
        val result = popup.showAndWait()

        val oldFile = File(currDir, listView.selectionModel.selectedItem)

        if(result.isPresent()) {
            val name = popup.getEditor().getText()
            val newFile = File(oldFile.getPath().replace(listView.selectionModel.selectedItem, name))
            val renamed = oldFile.renameTo(newFile)
            if (renamed) {
                reloadList(name)
                return null
            } else {
                return ""
            }
        }
        return null
    }

    fun toggleHidden(){

    }

    fun descend(){
        val directory = File(currDir.path + "\\" + listView.selectionModel.selectedItem)
        if(directory.isDirectory){
            val files = directory.listFiles().toList()
            listView.items.clear()
            for (file in files){
                listView.items.add(file.name)
            }
            if(listView.items.size > 0){
                listView.selectionModel.selectionMode = SelectionMode.SINGLE
                listView.selectionModel.select(0)
            }
            currDir = directory
        }
    }

    fun ascend(){
        val directory = currDir.parentFile
        if(directory.isDirectory){
            val files = directory.listFiles().toList()
            listView.items.clear()
            for (file in files){
                listView.items.add(file.name)
            }
            if(listView.items.size > 0){
                listView.selectionModel.selectionMode = SelectionMode.SINGLE
                listView.selectionModel.select(0)
            }
            currDir = directory
        }
    }

    fun home(){
        val files = homeDir.listFiles().toList()
        listView.items.clear()
        for (file in files){
            listView.items.add(file.name)
        }
        if(listView.items.size > 0){
            listView.selectionModel.selectionMode = SelectionMode.SINGLE
            listView.selectionModel.select(0)
        }
        currDir = homeDir
    }

    fun resizePreview(fileName: String){
        val file = File(currDir.path + "\\" + fileName)
        val ext = file.extension.lowercase()
        //check if unreadable here!
        layout.center = null
        if(ext == "txt" || ext == "md"){
            val textContainer = VBox()
            val textArea = TextArea()
            try {
                Scanner(file).use { input ->
                    while (input.hasNextLine()) {
                        textArea.appendText(input.nextLine() + "\n")
                    }
                }
            } catch (ex: FileNotFoundException) {
                textArea.appendText("File cannot be read")
            }
            textArea.isWrapText = true
            textContainer.children.add(textArea)
            textContainer.isFillWidth = true
            textArea.minHeight = 415.00
            textArea.minWidth = 550.00
            layout.center = textContainer
            textArea.focusTraversableProperty().set(false)
            textArea.isMouseTransparent = true;
            textArea.editableProperty().set(false)
        }
        else if(ext == "png" || ext == "jpg" || ext == "bmp") {
            val imageView = ImageView(Image(FileInputStream(file.path)))
            imageView.isPreserveRatio = true
            imageView.fitHeight = 415.00
            imageView.fitWidth = 550.00
            layout.center = imageView
        }
        val pathLabel = Label(file.path)
        pathLabel.padding = Insets(5.00)
        layout.bottom = pathLabel
    }
}

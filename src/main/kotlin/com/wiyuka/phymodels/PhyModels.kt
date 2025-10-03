package com.wiyuka.phymodels

import com.google.gson.Gson
import com.wiyuka.phymodels.bukkitlistener.BukkitListenerManager.Companion.registerBukkitListeners
import com.wiyuka.phymodels.bukkitlistener.DroppedItemListener
import com.wiyuka.phymodels.command.AddModel
import com.wiyuka.phymodels.command.AddScript
import com.wiyuka.phymodels.command.ClothCommand
import com.wiyuka.phymodels.command.GenerateModel
import com.wiyuka.phymodels.command.ModelInfo
import com.wiyuka.phymodels.command.PhyPerform
import com.wiyuka.phymodels.command.RemoveModel
import com.wiyuka.phymodels.command.ScriptStart
import com.wiyuka.phymodels.command.ToggleDebug
import com.wiyuka.phymodels.droppeditem.DroppedItem
import com.wiyuka.phymodels.listener.Listeners
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.cloth.Cloth
import com.wiyuka.phymodels.tools.Active
import com.wiyuka.phymodels.tools.AreaSelect
import com.wiyuka.phymodels.tools.Crawl
import com.wiyuka.phymodels.tools.LiquidActive
import com.wiyuka.phymodels.tools.P2PCreator
import electrostatic4j.snaploader.LibraryInfo
import electrostatic4j.snaploader.LoadingCriterion
import electrostatic4j.snaploader.NativeBinaryLoader
import electrostatic4j.snaploader.filesystem.DirectoryPath
import electrostatic4j.snaploader.platform.NativeDynamicLibrary
import electrostatic4j.snaploader.platform.util.PlatformPredicate
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class PhyModels : JavaPlugin() {

    companion object{
        val jsonEncoder: Gson = Gson()
        var debug = false
        lateinit var logger: Logger
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        // Plugin startup logic
        /*
        思路:
        1. 创建模型
        2. 以模型的坐标作为原点创建JBullet的世界
        3. 添加模型作为刚体到0,0,0
        4. 模型附近10*10*10的可以直接接触的方块以静态刚体,在相对模型坐标系下添加到模型的世界
        5. 当模型超过10*10*10的范围时,重复2~5的步骤
        更新过程:
        1. 判断是否有其他模型进入了当前模型的10*10*10范围并添加到当前模型的世界,更新已经在范围内的其他模型的位置
        2. 更新每个模型对应的JBullet世界(记录模拟世界在主世界中的坐标)
        3. 获取更新后的模型在主世界的坐标,移动模型
        每Tick更新一次
         */
        PhyModels.logger = logger
        PhyModels.plugin = this

        //加载模型
        initialize()
    }
    fun initialize(){
        logger.info("""
┏────────────────────────────────────────────────────┓
    ____  __          __  ___          __     __    
   / __ \/ /_  __  __/  |/  ____  ____/ ___  / _____
  / /_/ / __ \/ / / / /|_/ / __ \/ __  / _ \/ / ___/
 / ____/ / / / /_/ / /  / / /_/ / /_/ /  __/ (__  ) 
/_/   /_/ /_/\__, /_/  /_/\____/\__,_/\___/_/____/  
            /____/                                  
─────────────────────────────────────────────────────
                  author: wiyuka                      
┗────────────────────────────────────────────────────┛
""")

        logger.info("====== Config Initialize Start =======")
        config.addDefault("enableDroppedItem", true)
        config.options().copyDefaults(true)
        logger.info("====== Config Initialize Finish =======")
        saveConfig()

        logger.info("")
        logger.info("======= Physics world Initialize Start =======")
        loadBulletNativeLibrary()
        PhysicsTickManager.setup(this)
        logger.info("======= Physics world Load Finish =======")

        logger.info("")


        logger.info("======= Model Load Start =======")
        Model.setup(this)
        logger.info("======= Model Load Finish =======")

        logger.info("")
        getCommand("addmodel")?.setExecutor(AddModel(this))
        getCommand("generatemodel")?.setExecutor(GenerateModel())
        getCommand("addscript")?.setExecutor(AddScript())
        getCommand("scriptstart")?.setExecutor(ScriptStart())
        getCommand("removemodel")?.setExecutor(RemoveModel())
        getCommand("toggledebug")?.setExecutor(ToggleDebug())
        getCommand("phyperform")?.setExecutor(PhyPerform())
        getCommand("modelinfo")?.setExecutor(ModelInfo())
        getCommand("cloth")?.setExecutor(ClothCommand())

        registerBukkitListeners(this)
    }

    fun loadBulletNativeLibrary() {
        val info = LibraryInfo(null, "bulletjme", DirectoryPath.USER_DIR)
        val loader = NativeBinaryLoader(info)

        val libraries = arrayOf(
            NativeDynamicLibrary("native/linux/arm64", PlatformPredicate.LINUX_ARM_64),
            NativeDynamicLibrary("native/linux/arm32", PlatformPredicate.LINUX_ARM_32),
            NativeDynamicLibrary("native/linux/x86_64", PlatformPredicate.LINUX_X86_64),
            NativeDynamicLibrary("native/osx/arm64", PlatformPredicate.MACOS_ARM_64),
            NativeDynamicLibrary("native/osx/x86_64", PlatformPredicate.MACOS_X86_64),
            NativeDynamicLibrary("native/windows/x86_64", PlatformPredicate.WIN_X86_64)
        )

        loader.registerNativeLibraries(libraries)
            .initPlatformLibrary()
            .loadLibrary(LoadingCriterion.CLEAN_EXTRACTION)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

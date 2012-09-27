/**
 * Hook into the grails build process to generate a jar containing our M/R jobs. Hadoop needs jars to distribute
 * the code.
 * See "4.3 Hooking into Events" in the Grails Framework Reference.
 */

def JARFILE = 'lib/hbe.jar'

eventCompileEnd = { msg ->

  println ('   Create M/R Jars from ' + classesDir + " to " + JARFILE)
  // always overrides
  ant.jar(destFile: JARFILE, baseDir: classesDir, includes:'com/nnapz/hbaseexplorer/mr/**')
 
}

// todo: there should be a clean-hook

class UrlMappings {
    static mappings = {
      "/$controller/$action?/$id?"{
	      constraints {
			 // apply constraints here
		  }
	  }
      //"/"(view:"/index")
      "/" (controller:"hbaseSource", action: "list")
	  "500"(view:'/error')
	  
	}
}

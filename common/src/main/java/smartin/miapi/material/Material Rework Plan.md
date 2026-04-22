# Material Rework Plan
- restructure material class with controller classes, instead of extending those controller it should return those controller
  similar toRenderController
  - stat controller
  - client controller //store json, only run on client
    - move RenderController into here 
  - group controller
  - property controller
  - variant controller
  - ingredient controller

```json5
{
  "translation": "Custom Name",
  "stats": {
    "hardness": 5,
    ...
  },
  "render": {
    ...
  },
  "groups": {
    "hidden": [],
    "visible": []
  },
  "properties": {
    "visual-only": {
    },
    "hidden": {
    },
    "default": {
    }
  },
  "item": {
  },
  "variants": [
    ...
  ]
}
```

```json5
{
  "translation": "Custom Name",
  "stats": {
    "hardness": 5
  },
  "render": {
    "icon": {
      
    },
    "palette": {
      
    },
    "color": "#FFFFFFFF",
    "can_be_died": true,
    "dye_palette": {
      
    }
  },
  "groups": {
    "hidden": [],
    "visible": []
  },
  "properties": {
    "visual-only": {
      "module_tag": {
        "fire_proof": true
      }
    },
    "hidden": {
      
    },
    "default": {
      
    }
  },
  "item": {
    
  },
  "variants": [
    {
      "condition": {
        "type": true
      },
      "overwrite": {
        
      }
    }
  ]
}
```
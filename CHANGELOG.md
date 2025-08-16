## v2.0.13 (1.21)
### Performance :
- improved modular item checks by 50%
- improved decode performance by 90%
- improved encode performance by 50%
- improved rendering performance by ~20%
  
So, Performance seems to still be an issue, so if you encounter any issues create run spark profiler
(try to keep the profiler as contained on the issue as possible)
and either create a GH issue or ping me on discord with the spark link and a description of the issue.


- fixed luminous learning not affecting mob xp.
- fixed crash related to testing enchantability of invalid modular items
- fixed bug where items sometimes would not render on the bench
- fixed crossbow not shooting
- removed ht treechop compat log spam
- re-added epic fight compat
- fixed create options disappearing on reload
- fixed issues in enchanting logic
- fixed emi/jei/rei item tooltip rendering issue
- fixed crash related to skeletons
- fixed bug with create options where the top options would select the wrong thing.
- added support for module-ids as material property keys
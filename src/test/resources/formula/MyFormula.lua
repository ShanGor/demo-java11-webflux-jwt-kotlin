Dec = luajava.bindClass('java.math.BigDecimal')
Dec.mt={}

function Dec.new( a )
  setmetatable( a, Dec.mt )
  return a
end
Dec.mt.__call = function(tb, key) {
}
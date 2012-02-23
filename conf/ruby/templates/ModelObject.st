require_relative "../lib/monkey"

class $className$Model
  # properly structured attribute names from codegen
  attr_accessor $attributeMapping:{m | :$m.value$};separator=", "$

  # :internal => :external
  def self.attribute_map
    {
$attributeMapping:{m |        :$m.value$ => :$m.key$};separator=",\r"$
    }
  end


  def initialize(attributes = {})
    # Morph attribute keys into undescored rubyish style
    $className$Model.attribute_map.each_pair do |internal, external|
      if attributes.has_key? external
        attributes[internal] = attributes.delete(external)
      end
    end

    # Assign attributes
    attributes.each do |name, value|
      send("#{name}=", value) if self.respond_to?(name.to_sym)
    end
  end

  def to_body
    body = {}
    $className$Model.attribute_map.each_pair do |key,value|
      body[value] = self.send(key) unless self.send(key).nil?
    end
    body
  end
end


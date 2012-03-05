require 'spec_helper'

describe "Pet" do
  
  before do
    configure_swagger
  end
  
  describe "initialize" do
    
    it "mutates camelCase attribute keys to underscore" do
      pet = PetModel.new({:photoUrls => [1,2,3]})
      pet.photo_urls.should == [1,2,3]
    end

    it "leaves underscored attribute keys underscored" do
      pet = PetModel.new
      pet.photo_urls = [1,2,3]
      pet.photo_urls.should == [1,2,3]
    end
    
  end

  describe "to_body" do
    
    it "converts the attribute names back to their original form" do
      pet = PetModel.new({:photo_urls => [1,2,3]})
      pet.to_body[:photoUrls].should == [1,2,3]
    end
    
  end
  
  describe "static methods" do

    it "makes a request" do
      pet = Pet_api.get_pet_by_id(1)
      pet.id.should == 1
      pet.name.should == "Cat 1"
    end

    it "throws a ClientError on invalid pet request" do
      expect {
        pet = Pet_api.get_pet_by_id(0)
        raise "should have failed on invalid pet!".inspect
      }.to raise_error(ClientError, /Pet not found/i)
    end
    
    it "adds a pet" do 
      pet = PetModel.new({:id => 100, :name => "Gorilla"})
      Pet_api.add_pet(pet)
    end
  end

end

describe "Store" do
  describe "call store" do
    Swagger.configuration.api_key = 'special-key'
    item = Store_api.get_order_by_id(1)
    item.id.should == 1
    
  end
end
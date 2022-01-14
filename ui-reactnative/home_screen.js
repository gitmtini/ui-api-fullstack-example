import { StatusBar } from 'expo-status-bar';
import * as React from 'react';
import { useEffect, useState } from "react";
import { StyleSheet, Text, View, Button } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
//import { * as google } from '@types/google-protobuf';
import styles from './styles';
import { getCustomerDetails } from './apicaller';
import { CustomerDetails } from "./proto/lib/CustomerDetailsAndWallet_pb";


const HomeScreen = ({ navigation }) => {
    const [user, setUser] = useState([]);
    const [userName, setUserName] = useState(["empty"]);
    const [title, setTitle] = useState([" Projects "]);
    useEffect(() => {
      console.log("HomeScreen useEffect");
      getCustomerDetails().then((res)=> {
            //console.log(` homescreen res = ${res}`);
            //console.log(` homescreen res.userName = ${res.name}`);
            if(res){
            setUser(res);
            //console.log(` homescreen user = ${user}`);
            setUserName(res.name);
            //console.log(` homescreen setUserName = ${userName}`);
            setTitle(`Go to ${res.name} projects`);
            }
      });

    },[]);


  return (
   <View style={[styles.container, {
        // Try setting `flexDirection` to `"row"`.
        flexDirection: "column"
      }]}>

      <View style={{ flex: 2, alignItems: 'center', justifyContent: 'center', backgroundColor: '#ffa' }}>
        <Text>Open up App.js to start working on your app! Lets do this again ! </Text>
        <Text>Welcome {user.name} </Text>
        <Text>Phone: {user.telephone} </Text>
         <Text>email: {user.email} </Text>
        <StatusBar style="auto" />
      </View>
      <Button
            title="Jump to {user.name} profile"
            onPress={() =>
              navigation.navigate('Profile', { name: 'Jane' })
            }
          />

      <Button
            title= "Project details"//{title}
            onPress={() =>
              navigation.navigate('Projects', user)
            }
          />
    </View>
  );
}

export default HomeScreen;
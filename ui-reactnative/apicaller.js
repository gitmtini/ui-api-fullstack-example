
import { useEffect, useState } from "react";
import { CustomerDetails } from "./proto/lib/CustomerDetailsAndWallet_pb";
import * as protobuf from "protobufjs";
import base64 from 'react-native-base64'

const api_url = 'http://localhost:80/awesome/api/v1';

const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
const atob = (input = '') => {
	let str = input.replace(/=+$/, '');
	let output = '';

	if (str.length % 4 == 1) {
		throw new Error("'atob' failed: The string to be decoded is not correctly encoded.");
	}
	for (let bc = 0, bs = 0, buffer, i = 0;
		buffer = str.charAt(i++);

		~buffer && (bs = bc % 4 ? bs * 64 + buffer : buffer,
			bc++ % 4) ? output += String.fromCharCode(255 & bs >> (-2 * bc & 6)) : 0
	) {
		buffer = chars.indexOf(buffer);
	}

	return output;
}

FileReader.prototype.readAsArrayBuffer = function (blob) {
    return new Promise(function(resolve, reject) {
	let retVal = null;
	const fr = new FileReader();
	fr.onloadend = () => {
		const content = atob(fr.result.substr("data:application/json;base64,".length));
		//console.log(`content = ${content}`)
		const buffer = new ArrayBuffer(content.length);
		const view = new Uint8Array(buffer);
		view.set(Array.from(content).map(c => c.charCodeAt(0)));
		//console.log(`view = ${view}`)
		//console.log(this);
        retVal = view;
        //console.log(`retVal=${retVal}`);
	};
	fr.readAsDataURL(blob);
        setTimeout(() => {
          resolve(retVal);
       }, 1000);

    })//promise

}

const getCustomerDetails = async ()=>{
    console.log("apicaller getCustomerDetails");
    var protobuf = require("protobufjs/minimal");
    //var fr = require(FileReader);
    try{

        const requestOptions = {
            method: 'GET',
            headers: { 'Content-Type': 'application/octet-stream'}
            //, body: JSON.stringify({ title: 'React POST Request Example' })
        };

        //console.log(`apicaller getCustomerDetails url  ${api_url}`);
        const response = await fetch(`${api_url}/customer/123`, requestOptions);
        //console.log(`apicaller getCustomerDetails response = ${response}`);
        const blob = await response.blob();
        //console.log(blob);

        const fr = new FileReader();
        let res = await fr.readAsArrayBuffer(blob);
        //var res = await FileReader.prototype.readAsArrayBuffer(blob);
        //console.log(`res = ${res}`);
        const customer = CustomerDetails.deserializeBinary(res).toObject();
        console.log(`apicaller getCustomerDetails customer = ${customer}`);

        return await customer;
    }catch(error){
        console.error(error);
    }
}

export { getCustomerDetails  };
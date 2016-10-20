import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server

var prevCmd
var tryAgain

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username>')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    server = connect({ host: 'localhost', port: 8080 }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
    	
      var msg= Message.fromJSON(buffer)
      
      
      if(msg.command === '')
    	  {
    	  this.log('no command')
    	  }
      
      
      if(msg.command.charAt(0) === '@')
    	  {
    	  this.log(cli.chalk['magenta'](msg.toString()))
    	  }
      
      switch(msg.command) {
    case 'connect':
    	this.log(cli.chalk['bgRed'](msg.toString()))
        break;
    case 'disconnect':
    	this.log(cli.chalk['bgRed'](msg.toString()))
        break;
    case 'broadcast':
    	this.log(cli.chalk['bgGreen'](msg.toString()))
    	break;
    case 'echo':
    	this.log(cli.chalk['green'] (msg.toString()))
    	break;
    case 'users':
    	this.log(cli.chalk['blue'] (msg.toString()))
    	break;
    	

      }//end switch
      
      
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[^, ]+/g) //no idea the second param. played with repel on lodash and it works so fuck it
    const contents = rest.join(' ')

    let cmd = command //command is readonly?
        
    do{
    	console.log("in do")
    	console.log("prev cmd:" + prevCmd)
    	console.log("cmd: " + cmd)
    	console.log("tryAgain:" + tryAgain)
    	tryAgain = false
    	
    
    if (cmd === 'disconnect') {
    	prevCmd = command;
      server.end(new Message({ username, command }).toJSON() + '\n')
      
    } else if (cmd === 'echo') {
    	prevCmd = command;
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } 
    
    else if (cmd === 'users') {
    	prevCmd = command;
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      } 
    
    else if (cmd === 'broadcast') {
    	prevCmd = command;
    	server.write(new Message({ username, command, contents }).toJSON() + '\n')	
    }
    
    else if (cmd.charAt(0) === '@') {
    	prevCmd = command;
    	server.write(new Message({ username, command, contents }).toJSON() + '\n')	
    }
       //undefined, null idk. f u javascript! ughhhh check both
    else if(prevCmd != undefined || prevCmd != null){
    	cmd = prevCmd   //need to loop back over and check again
    	console.log("inside else if prev != null")
    	tryAgain = true
    }
    
    else {
      this.log(`Command <${command}> was not recognized`)
      //cli.chalk['green']
    }
    
    } while(tryAgain)

    callback()
  })

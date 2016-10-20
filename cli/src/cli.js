import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server

let prevCmd

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
    	

      }// end switch
      
      
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[^, ]+/g) // no idea the
															// second param.
															// played with repel
															// on lodash and it
															// works so fuck it
    const contents = rest.join(' ')

    let cmd = command // command is readonly?
         	
    
    if (cmd === 'disconnect') {
    	prevCmd = command;
      server.end(new Message({ username, command }).toJSON() + '\n')
      
    } else if (cmd === 'echo') {
    	prevCmd = command;
    	
    	console.log('1st echo')
   	 	console.log(username)
   	 	console.log(command)
   	 	console.log(contents)
    	
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } 
    
    else if (cmd === 'users') {
    	prevCmd = command;
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      } 
    
    else if (cmd === 'broadcast') {
    	prevCmd = command;
    	
    	console.log('1st broadcast')
   	 	console.log(username)
   	 	console.log(command)
   	 	console.log(contents)
    	
    	server.write(new Message({ username, command, contents }).toJSON() + '\n')	
    }
    
    else if (cmd.charAt(0) === '@') {
    	prevCmd = command;
    	server.write(new Message({ username, command, contents }).toJSON() + '\n')	
    }

    else if(prevCmd != undefined)
    {
    	
    	// //////////////////////////////
    	const newContents = command + ' ' + contents
    	// ///////////////////////////////
    	
    	
    	  if (prevCmd === 'echo') {
            server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')
        }   
        else if (prevCmd === 'users') {
            server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')
          } 
        else if (prevCmd === 'broadcast') {	 	
        	server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')	
        }
        else if (prevCmd.charAt(0) === '@') {
        	server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')	
        }
    	
    } // inner if/elseIf
    
    else {
      this.log(`Command <${command}> was not recognized`)
      // cli.chalk['green']
    }
    

    callback()
  })

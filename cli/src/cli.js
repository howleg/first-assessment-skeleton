import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server

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
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input) //no idea the second param. played with repel on lodash and it works so fuck it
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } 
    
    else if (command === 'users') {
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      } 
    
    else if (command === 'broadcast') {
    	//cli.log(cli.chalk['red'](l))
    	server.write(new Message({ username, command, contents }).toJSON() + '\n')
    	
    }
    
    
    else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })

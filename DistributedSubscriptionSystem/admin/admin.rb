require 'socket'

class AdminClient
  SERVER1_HOST = 'localhost'
  SERVER2_HOST = 'localhost'
  SERVER3_HOST = 'localhost'

  SERVER1_ADMIN_PORT = 1122
  SERVER2_ADMIN_PORT = 1123
  SERVER3_ADMIN_PORT = 1124

  def initialize
    # Kullanıcıdan veri alırken gets kullanacağız, Scanner'a gerek yok
  end

  def start
    loop do
      puts "Admin Client - Sunucuya Bağlanmak için komut girin (STRT, CPCTY) veya Çıkmak için 'exit': "
      command = gets.chomp

      break if command.downcase == 'exit'

      # Eğer komut STRT ise, tüm sunuculara başlatma komutunu gönderiyoruz
      if command.downcase == 'strt'
        puts "Tüm sunucuları başlatıyorum..."
        start_all_servers
      else
        puts "Hedef Sunucu Seçin (1, 2, 3): "
        server_choice = gets.to_i

        target_server = get_target_server(server_choice)

        if target_server
          admin_port = get_admin_port(server_choice)
          send_request_to_server(command, target_server, admin_port)
        else
          puts "Geçersiz sunucu seçimi!"
        end
      end
    end
  end

  private

  def get_target_server(server_choice)
    case server_choice
    when 1
      SERVER1_HOST
    when 2
      SERVER2_HOST
    when 3
      SERVER3_HOST
    else
      nil
    end
  end

  def get_admin_port(server_choice)
    case server_choice
    when 1
      SERVER1_ADMIN_PORT
    when 2
      SERVER2_ADMIN_PORT
    when 3
      SERVER3_ADMIN_PORT
    else
      -1
    end
  end

  def send_request_to_server(command, server_host, server_port)
    begin
      socket = TCPSocket.new(server_host, server_port)
      message = Message.new(command, "İçerik")  # basit bir örnek
      socket.puts message.to_s

      response = socket.gets
      puts "Sunucudan gelen yanıt: #{response}"
    rescue => e
      puts e.message
    ensure
      socket.close if socket
    end
  end

  # Tüm sunucuları başlatmak için bu metodu ekledik
  def start_all_servers
    # 3 sunucuya da STRT komutu gönderiyoruz
    [SERVER1_HOST, SERVER2_HOST, SERVER3_HOST].each_with_index do |server, index|
      admin_port = get_admin_port(index + 1)
      send_request_to_server('STRT', server, admin_port)
    end
  end
end

class Message
  attr_reader :demand, :content

  def initialize(demand, content)
    @demand = demand
    @content = content
  end

  def to_s
    "Message{demand='#{@demand}', content='#{@content}'}"
  end
end

client = AdminClient.new
client.start

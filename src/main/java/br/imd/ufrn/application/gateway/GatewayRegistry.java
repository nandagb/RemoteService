package br.imd.ufrn.application.gateway;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import br.imd.ufrn.application.models.ServiceRecord;

public class GatewayRegistry {
    private final ConcurrentHashMap<String, ServiceRecord> servicesTable;
    private final AtomicInteger index = new AtomicInteger(0);
    // private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private int heartBeatTimeout = 9000;
    private int failureDetectorInterval = 3000;

    public GatewayRegistry() {
        servicesTable = new ConcurrentHashMap<>();
        
        new Thread(() -> this.failureDetector()).start();
    }

    public void failureDetector() {
        while (true) {
            synchronized (servicesTable) {
                for (HashMap.Entry<String, ServiceRecord> entry : servicesTable.entrySet()) {
                    // String key = entry.getKey();
                    ServiceRecord service = entry.getValue();

                    if (System.currentTimeMillis() - service.getLastHeartbeat() > this.heartBeatTimeout && service.getStatus()) {
                        System.out.println("Servidor de porta: " + service.getPort() + " morreu");
                        service.setStatus(false);
                        // Remove dead service from registry
                        // servicesTable.remove(entry.getKey());
                    }
                }
            }

            try {
                Thread.sleep(this.failureDetectorInterval);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException: erro ao chamar o sleep do failureDetector");
            }

        }
    }

    public ServiceRecord getNextService() {
        List<ServiceRecord> services = servicesTable.values()
        .stream()
        .filter(ServiceRecord::getStatus)
        .collect(Collectors.toList());

        if (services.isEmpty()) return null;

        int size = services.size();
        int i = this.index.getAndUpdate(v -> (v + 1) % size);

        // old value of index
        return services.get(i);
    }

    public synchronized void update(String key, ServiceRecord service) {
        // System.out.println("dentro de update: " + key + " service status: " + service.getStatus());
        ServiceRecord tableService = servicesTable.get(key);

        if (tableService == null) {
            System.out.println("Servidor de porta: " + service.getPort() + " iniciado");
            servicesTable.put(key, service);
            tableService = service;
        }

        if (!tableService.getStatus()) {
            System.out.println("Servidor de porta: " + tableService.getPort() + " iniciado");
        }

        tableService.refreshHeartBeat();
    }
    public void remove(int port) {
        // Remove service entries matching the given port
        servicesTable.entrySet().removeIf(entry -> entry.getValue().getPort() == port);
    }
}

